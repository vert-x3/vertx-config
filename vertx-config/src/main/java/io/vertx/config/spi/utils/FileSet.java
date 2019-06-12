/*
 * Copyright (c) 2014 Red Hat, Inc. and others
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package io.vertx.config.spi.utils;

import io.vertx.config.spi.ConfigProcessor;
import io.vertx.core.*;
import io.vertx.core.impl.launcher.commands.FileSelector;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;

/**
 * Utility class to manage file set selected using a pattern.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class FileSet {

  private final static Logger LOGGER = LoggerFactory.getLogger(FileSet.class);

  private final String pattern;
  private final ConfigProcessor processor;
  private final File root;
  private final Vertx vertx;
  private final Boolean rawData;

  /**
   * Creates a new {@link FileSet} from a json object.
   *
   * @param vertx the Vert.x instance
   * @param root  the root of the fileset (directory)
   * @param set   the configuration
   */
  public FileSet(Vertx vertx, File root, JsonObject set) {
    this.vertx = vertx;
    this.root = root;
    this.pattern = set.getString("pattern");
    if (this.pattern == null) {
      throw new IllegalArgumentException("Each file set needs to contain a `pattern`");
    }
    this.rawData = set.getBoolean("raw-data", false);
    String format = set.getString("format", "json");
    this.processor = Processors.get(format);
    if (this.processor == null) {
      throw new IllegalArgumentException("Unknown configuration format `" + format + "`, supported types are " +
        Processors.getSupportedFormats());
    }
  }

  private boolean matches(String path) {
    return FileSelector.match(pattern, path, false);
  }

  /**
   * Iterates over the given set of files, and for each matching file, computes the resulting configuration. The
   * given handler is called with the merged configuration (containing the configuration obtained by merging the
   * configuration from all matching files).
   *
   * @param files   the list of files
   * @param handler the handler called with the computed configuration
   */
  public void buildConfiguration(List<File> files, Handler<AsyncResult<JsonObject>> handler) {
    List<Future> futures = new ArrayList<>();

    files.stream()
      .map(file -> {
        String relative = null;
        if (file.getAbsolutePath().startsWith(root.getAbsolutePath())) {
          relative = file.getAbsolutePath().substring(root.getAbsolutePath().length() + 1);
        }
        if (relative == null) {
          LOGGER.warn("The file `" + file.getAbsolutePath() + "` is not in '" + root
            .getAbsolutePath() + "'");
        }
        return relative;
      })
      .filter(Objects::nonNull)
      .filter(this::matches)
      .map(s -> new File(root, s))
      .forEach(file -> {
        Promise<JsonObject> promise = Promise.promise();
        futures.add(promise.future());
        try {
          vertx.fileSystem().readFile(file.getAbsolutePath(),
            buffer -> {
              if (buffer.failed()) {
                promise.fail(buffer.cause());
              } else {
                processor.process(vertx, new JsonObject().put("raw-data", rawData), buffer.result(), promise);
              }
            });
        } catch (RejectedExecutionException e) {
          // May happen because ot the internal thread pool used in the async file system.
          promise.fail(e);
        }
      });

    CompositeFuture.all(futures).setHandler(ar -> {
      if (ar.failed()) {
        handler.handle(Future.failedFuture(ar.cause()));
      } else {
        // Merge
        JsonObject result = new JsonObject();
        futures.stream()
          .map(future -> (JsonObject) future.result())
          .forEach(result::mergeIn);
        handler.handle(Future.succeededFuture(result));
      }
    });
  }

  /**
   * List all the files from a directory (recursive)
   *
   * @param root the root
   * @return the list of files
   */
  public static List<File> traverse(File root) {
    List<File> files = new ArrayList<>();
    if (!root.isDirectory()) {
      return files;
    } else {
      File[] children = root.listFiles();
      if (children == null) {
        return files;
      } else {
        for (File file : children) {
          if (file.isDirectory()) {
            files.addAll(traverse(file));
          } else {
            files.add(file);
          }
        }
      }
      return files;
    }
  }
}

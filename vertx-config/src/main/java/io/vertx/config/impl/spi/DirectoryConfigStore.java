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

package io.vertx.config.impl.spi;

import io.vertx.config.spi.ConfigStore;
import io.vertx.config.spi.utils.FileSet;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A configuration store loading a set of files from a directory.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class DirectoryConfigStore implements ConfigStore {

  private VertxInternal vertx;

  private File path;
  private final List<FileSet> filesets = new ArrayList<>();

  public DirectoryConfigStore(Vertx vertx, JsonObject configuration) {
    this.vertx = (VertxInternal) vertx;
    String thePath = configuration.getString("path");
    if (thePath == null) {
      throw new IllegalArgumentException("The `path` configuration is required.");
    }
    this.path = new File(thePath);
    if (this.path.isFile()) {
      throw new IllegalArgumentException("The `path` must not be a file");
    }

    JsonArray files = configuration.getJsonArray("filesets");
    if (files == null) {
      throw new IllegalArgumentException("The `filesets` element is required.");
    }

    for (Object o : files) {
      JsonObject json = (JsonObject) o;
      FileSet set = new FileSet(vertx, this.path, json);
      this.filesets.add(set);
    }
  }

  @Override
  public Future<Buffer> get() {
    return vertx.<List<File>>executeBlocking(promise -> {
      try {
        promise.complete(FileSet.traverse(path).stream().sorted().collect(Collectors.toList()));
      } catch (Throwable e) {
        promise.fail(e);
      }
    }).flatMap(files -> {
      List<Future> futures = new ArrayList<>();
      for (FileSet set : filesets) {
        Promise<JsonObject> promise = vertx.promise();
        set.buildConfiguration(files, json -> {
          if (json.failed()) {
            promise.fail(json.cause());
          } else {
            promise.complete(json.result());
          }
        });
        futures.add(promise.future());
      }
      return CompositeFuture.all(futures);
    }).map(compositeFuture -> {
      JsonObject json = new JsonObject();
      compositeFuture.<JsonObject>list().forEach(config -> json.mergeIn(config, true));
      return json.toBuffer();
    });
  }

  @Override
  public Future<Void> close() {
    return vertx.getOrCreateContext().succeededFuture();
  }
}

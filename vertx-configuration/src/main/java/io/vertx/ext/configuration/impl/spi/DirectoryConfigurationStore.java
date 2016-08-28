package io.vertx.ext.configuration.impl.spi;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.configuration.spi.ConfigurationStore;
import io.vertx.ext.configuration.utils.FileSet;

import java.io.File;
import java.util.*;

/**
 * A configuration store loading a set of files from a directory.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class DirectoryConfigurationStore implements ConfigurationStore {

  private Vertx vertx;

  private File path;
  private final List<FileSet> filesets = new ArrayList<>();

  public DirectoryConfigurationStore(Vertx vertx, JsonObject configuration) {
    this.vertx = vertx;
    String path = configuration.getString("path");
    if (path == null) {
      throw new IllegalArgumentException("The `path` configuration is required.");
    }
    this.path = new File(path);
    if (this.path.isFile()) {
      throw new IllegalArgumentException("The `path` must not be a file");
    }

    JsonArray filesets = configuration.getJsonArray("filesets");
    if (filesets == null) {
      throw new IllegalArgumentException("The `filesets` element is required.");
    }

    for (Object o : filesets) {
      JsonObject json = (JsonObject) o;
      FileSet set = new FileSet(vertx, this.path, json);
      this.filesets.add(set);
    }
  }

  private List<File> traverse(File root) {
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

  @Override
  public void get(Handler<AsyncResult<Buffer>> completionHandler) {

    vertx.<List<File>>executeBlocking(
        fut -> {
          try {
            fut.complete(traverse(path));
          } catch (Throwable e) {
            fut.fail(e);
          }
        },
        ar -> {
          if (ar.failed()) {
            completionHandler.handle(Future.failedFuture(ar.cause()));
          } else {
            List<Future> futures = new ArrayList<>();
            for (FileSet set : filesets) {
              Future<JsonObject> future = Future.future();
              set.buildConfiguration(ar.result(), json -> {
                if (json.failed()) {
                  future.fail(json.cause());
                } else {
                  future.complete(json.result());
                }
              });
              futures.add(future);
            }

            CompositeFuture.all(futures).setHandler(cf -> {
              if (cf.failed()) {
                completionHandler.handle(Future.failedFuture(cf.cause()));
              } else {
                JsonObject json = new JsonObject();
                futures.stream().map(f -> (JsonObject) f.result())
                    .forEach(json::mergeIn);
                completionHandler.handle(Future.succeededFuture(Buffer.buffer(json.encode())));
              }
            });
          }
        }
    );
  }
}

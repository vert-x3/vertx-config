package io.vertx.ext.configuration.impl.spi;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.launcher.commands.FileSelector;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.configuration.spi.ConfigurationProcessor;
import io.vertx.ext.configuration.spi.ConfigurationStore;
import io.vertx.ext.configuration.spi.ConfigurationStoreFactory;

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
  private Map<String, ConfigurationProcessor> processors = new HashMap<>();
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

    ServiceLoader<ConfigurationProcessor> processorImpl =
        ServiceLoader.load(ConfigurationProcessor.class,
            ConfigurationStoreFactory.class.getClassLoader());
    processorImpl.iterator().forEachRemaining(processor -> processors.put(processor.name(), processor));

    for (Object o : filesets) {
      JsonObject json = (JsonObject) o;
      FileSet set = new FileSet(json);
      this.filesets.add(set);
    }
  }

  private class FileSet {
    private final String pattern;
    private final ConfigurationProcessor processor;

    private FileSet(JsonObject set) {
      this.pattern = set.getString("pattern");
      if (this.pattern == null) {
        throw new IllegalArgumentException("Each file set needs to contain a `pattern`");
      }
      String format = set.getString("format", "json");
      this.processor = getConfigurationProcessor(format);
      if (this.processor == null) {
        throw new IllegalArgumentException("Unknown configuration format `" + format + "`, supported types are " +
            processors.keySet());
      }
    }

    private boolean matches(File file) {
      String relative = null;

      if (file.getAbsolutePath().startsWith(path.getAbsolutePath())) {
        relative = file.getAbsolutePath().substring(path.getAbsolutePath().length() + 1);
      }

      if (relative == null) {
        throw new IllegalArgumentException("The file `" + file.getAbsolutePath() + "` is not in '" + path
            .getAbsolutePath() + "'");
      }

      return FileSelector.match(pattern, relative, false);
    }

    private void scan(List<File> files, Handler<AsyncResult<JsonObject>> handler) {
      List<Future> futures = new ArrayList<>();
      files.stream()
          .filter(this::matches)
          .forEach(file -> {
            Future<JsonObject> future = Future.future();
            futures.add(future);

            vertx.fileSystem().readFile(file.getAbsolutePath(),
                buffer -> {
                  if (buffer.failed()) {
                    future.fail(buffer.cause());
                  } else {
                    processor.process(vertx, null, buffer.result(), future.completer());
                  }
                });
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
  }

  private ConfigurationProcessor getConfigurationProcessor(String format) {
    return processors.get(format);
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
              set.scan(ar.result(), json -> {
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

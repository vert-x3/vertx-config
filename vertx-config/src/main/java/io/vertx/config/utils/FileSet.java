package io.vertx.config.utils;

import io.vertx.core.*;
import io.vertx.core.impl.launcher.commands.FileSelector;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.config.spi.ConfigProcessor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
        .filter(s -> s != null)
        .filter(this::matches)
        .map(s -> new File(root, s))
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

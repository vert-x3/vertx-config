package io.vertx.config.impl.spi;

import io.vertx.config.spi.ConfigStore;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

/**
 * A configuration store loading a file.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class FileConfigStore implements ConfigStore {
  private Vertx vertx;

  private String path;

  public FileConfigStore(Vertx vertx, JsonObject configuration) {
    this.vertx = vertx;
    this.path = configuration.getString("path");
    if (this.path == null) {
      throw new IllegalArgumentException("The `path` configuration is required.");
    }
  }

  @Override
  public void get(Handler<AsyncResult<Buffer>> completionHandler) {
    vertx.fileSystem().readFile(path, completionHandler);
  }
}

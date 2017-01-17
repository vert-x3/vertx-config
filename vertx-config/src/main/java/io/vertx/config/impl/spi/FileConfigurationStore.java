package io.vertx.config.impl.spi;

import io.vertx.config.spi.ConfigurationStore;
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
public class FileConfigurationStore implements ConfigurationStore {
  private Vertx vertx;

  private String path;

  public FileConfigurationStore(Vertx vertx, JsonObject configuration) {
    this.vertx = vertx;
    this.path = configuration.getString("path");
    if (this.path == null) {
      throw new IllegalArgumentException("The `path` configuration is required.");
    }
  }

  @Override
  public void get(Handler<AsyncResult<Buffer>> completionHandler) {
    vertx.fileSystem().readFile(path, ar -> {
      if (ar.failed()) {
        completionHandler.handle(Future.failedFuture(ar.cause()));
      } else {
        try {
          completionHandler.handle(Future.succeededFuture(ar.result()));
        } catch (Exception e) {
          completionHandler.handle(Future.failedFuture(e));
        }
      }
    });
  }
}

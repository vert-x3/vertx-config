package io.vertx.ext.configuration.spi;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

/**
 * Defines a configuration store.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public interface ConfigurationStore {

  /**
   * Closes the configuration store.
   *
   * @param completionHandler handler called when the cleanup has been completed
   */
  default void close(Handler<Void> completionHandler) {
    completionHandler.handle(null);
  }

  /**
   * Retrieves the configuration store in this store.
   *
   * @param completionHandler the handler to pass the configuration
   */
  void get(Handler<AsyncResult<Buffer>> completionHandler);

}

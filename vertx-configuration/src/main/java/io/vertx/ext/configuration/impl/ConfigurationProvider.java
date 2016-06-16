package io.vertx.ext.configuration.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.configuration.spi.ConfigurationProcessor;
import io.vertx.ext.configuration.spi.ConfigurationStore;

/**
 * A configuration provider retrieve the configuration from a store and transform it to Json.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ConfigurationProvider {

  private ConfigurationStore store;

  private ConfigurationProcessor processor;

  public ConfigurationProvider(ConfigurationStore store, ConfigurationProcessor processor) {
    this.store = store;
    this.processor = processor;
  }

  void get(Vertx vertx, Handler<AsyncResult<JsonObject>> completionHandler) {
    store.get(maybeBuffer -> {
      if (maybeBuffer.failed()) {
        completionHandler.handle(Future.failedFuture(maybeBuffer.cause()));
      } else {
        processor.process(vertx, maybeBuffer.result(), maybeJson -> {
          if (maybeJson.failed()) {
            completionHandler.handle(Future.failedFuture(maybeJson.cause()));
          } else {
            completionHandler.handle(Future.succeededFuture(maybeJson.result()));
          }
        });
      }
    });
  }

}

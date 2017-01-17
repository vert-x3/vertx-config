package io.vertx.config.impl;

import io.vertx.config.spi.ConfigurationStore;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.config.spi.ConfigurationProcessor;

/**
 * A configuration provider retrieve the configuration from a store and transform it to Json.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ConfigurationProvider {

  private final JsonObject configuration;

  private final ConfigurationStore store;

  private final ConfigurationProcessor processor;

  public ConfigurationProvider(ConfigurationStore store, ConfigurationProcessor processor, JsonObject config) {
    this.store = store;
    this.processor = processor;
    if (config == null) {
      this.configuration = new JsonObject();
    } else {
      this.configuration = config;
    }
  }

  void get(Vertx vertx, Handler<AsyncResult<JsonObject>> completionHandler) {
    store.get(maybeBuffer -> {
      if (maybeBuffer.failed()) {
        completionHandler.handle(Future.failedFuture(maybeBuffer.cause()));
      } else {
        processor.process(vertx, configuration, maybeBuffer.result(), maybeJson -> {
          if (maybeJson.failed()) {
            completionHandler.handle(Future.failedFuture(maybeJson.cause()));
          } else {
            completionHandler.handle(Future.succeededFuture(maybeJson.result()));
          }
        });
      }
    });
  }

  void close(Handler<Void> handler) {
    store.close(handler);
  }

}

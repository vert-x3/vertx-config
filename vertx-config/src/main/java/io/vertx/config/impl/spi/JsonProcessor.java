package io.vertx.config.impl.spi;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.config.spi.ConfigurationProcessor;

/**
 * Builds a json object from the given buffer.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class JsonProcessor implements ConfigurationProcessor {

  @Override
  public void process(Vertx vertx, JsonObject configuration, Buffer input, Handler<AsyncResult<JsonObject>> handler) {
    try {
      JsonObject json = input.toJsonObject();
      if (json == null) {
        json = new JsonObject();
      }
      handler.handle(Future.succeededFuture(json));
    } catch (Exception e) {
      handler.handle(Future.failedFuture(e));
    }
  }

  @Override
  public String name() {
    return "json";
  }
}

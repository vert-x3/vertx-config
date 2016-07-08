package io.vertx.ext.configuration.impl.spi;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.configuration.spi.ConfigurationProcessor;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class RawProcessor implements ConfigurationProcessor {
  @Override
  public String name() {
    return "raw";
  }

  @Override
  public void process(Vertx vertx, JsonObject configuration, Buffer input, Handler<AsyncResult<JsonObject>> handler) {
    String key = configuration.getString("raw.key");
    String type = configuration.getString("raw.type", "string");
    if (key == null) {
      handler.handle(Future.failedFuture("The `raw.key` is required in the configuration when using the `raw` " +
          "processor."));
    } else {
      JsonObject json = new JsonObject();
      try {
        switch (type) {
          case "string":
            json.put(key, input.toString(configuration.getString("raw.encoding", "utf-8")));
            handler.handle(Future.succeededFuture(json));
            break;
          case "json-object":
            json.put(key, input.toJsonObject());
            handler.handle(Future.succeededFuture(json));
            break;
          case "json-array":
            json.put(key, input.toJsonArray());
            handler.handle(Future.succeededFuture(json));
            break;
          case "binary":
            json.put(key, input.getBytes());
            handler.handle(Future.succeededFuture(json));
            break;
          default:
            handler.handle(Future.failedFuture("Unrecognized `raw.type` : " + type));
        }
      } catch (Exception e) {
        handler.handle(Future.failedFuture(e));
      }
    }
  }
}

package io.vertx.config.spi;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public interface ConfigProcessor {

  String name();

  void process(Vertx vertx, JsonObject configuration, Buffer input, Handler<AsyncResult<JsonObject>> handler);

}

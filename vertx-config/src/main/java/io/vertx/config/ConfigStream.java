package io.vertx.config;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@VertxGen
public interface ConfigStream extends ReadStream<JsonObject> {

  @Override
  ConfigStream exceptionHandler(Handler<Throwable> handler);

  @Override
  ConfigStream handler(Handler<JsonObject> handler);

  @Override
  ConfigStream endHandler(Handler<Void> endHandler);
}

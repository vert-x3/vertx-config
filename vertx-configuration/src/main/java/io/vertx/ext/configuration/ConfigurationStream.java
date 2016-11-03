package io.vertx.ext.configuration;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@VertxGen
public interface ConfigurationStream extends ReadStream<JsonObject> {

  @Override
  ConfigurationStream exceptionHandler(Handler<Throwable> handler);

  @Override
  ConfigurationStream handler(Handler<JsonObject> handler);

  @Override
  ConfigurationStream endHandler(Handler<Void> endHandler);
}

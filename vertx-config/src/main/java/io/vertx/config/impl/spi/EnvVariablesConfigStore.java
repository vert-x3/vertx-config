package io.vertx.config.impl.spi;

import io.vertx.config.spi.ConfigStore;
import io.vertx.config.spi.utils.JsonObjectHelper;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.config.spi.ConfigStoreFactory;

import java.util.Map;

/**
 * An implementation of configuration store loading the content from the environment variables.
 * <p>
 * As this configuration store is a singleton, the factory returns always the same instance.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class EnvVariablesConfigStore implements ConfigStoreFactory, ConfigStore {
  private final boolean rawData;

  private JsonObject cached;

  public EnvVariablesConfigStore() {
    this(false);
  }

  public EnvVariablesConfigStore(boolean rawData) {
    this.rawData = rawData;
  }

  @Override
  public String name() {
    return "env";
  }

  @Override
  public ConfigStore create(Vertx vertx, JsonObject configuration) {
    return new EnvVariablesConfigStore(configuration.getBoolean("raw-data", false));
  }

  @Override
  public void get(Handler<AsyncResult<Buffer>> completionHandler) {
    if (cached == null) {
      cached = all(System.getenv(), rawData);
    }
    completionHandler.handle(Future.succeededFuture(Buffer.buffer(cached.encode())));
  }

  private static JsonObject all(Map<String, String> env, boolean rawData) {
    JsonObject json = new JsonObject();
    env.entrySet().stream()
        .forEach(entry -> JsonObjectHelper.put(json, entry.getKey(), entry.getValue(), rawData));
    return json;
  }

}

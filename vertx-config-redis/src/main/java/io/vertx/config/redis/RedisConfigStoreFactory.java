package io.vertx.config.redis;

import io.vertx.config.spi.ConfigStore;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.config.spi.ConfigStoreFactory;

/**
 * Factory to create {@link RedisConfigStore} instances.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class RedisConfigStoreFactory implements ConfigStoreFactory {
  @Override
  public String name() {
    return "redis";
  }

  @Override
  public ConfigStore create(Vertx vertx, JsonObject configuration) {
    return new RedisConfigStore(vertx, configuration);
  }
}

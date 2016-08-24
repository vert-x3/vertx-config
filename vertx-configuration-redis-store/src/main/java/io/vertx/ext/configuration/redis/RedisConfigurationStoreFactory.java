package io.vertx.ext.configuration.redis;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.configuration.spi.ConfigurationStore;
import io.vertx.ext.configuration.spi.ConfigurationStoreFactory;

/**
 * Factory to create {@link RedisConfigurationStore} instances.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class RedisConfigurationStoreFactory implements ConfigurationStoreFactory {
  @Override
  public String name() {
    return "redis";
  }

  @Override
  public ConfigurationStore create(Vertx vertx, JsonObject configuration) {
    return new RedisConfigurationStore(vertx, configuration);
  }
}

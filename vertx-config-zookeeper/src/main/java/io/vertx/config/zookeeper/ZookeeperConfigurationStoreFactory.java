package io.vertx.config.zookeeper;

import io.vertx.config.spi.ConfigurationStore;
import io.vertx.config.spi.ConfigurationStoreFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Factory to create {@link ZookeeperConfigurationStore} instances.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ZookeeperConfigurationStoreFactory implements ConfigurationStoreFactory {
  @Override
  public String name() {
    return "zookeeper";
  }

  @Override
  public ConfigurationStore create(Vertx vertx, JsonObject configuration) {
    return new ZookeeperConfigurationStore(vertx, configuration);
  }
}

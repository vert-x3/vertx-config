package io.vertx.ext.configuration.zookeeper;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.configuration.spi.ConfigurationStore;
import io.vertx.ext.configuration.spi.ConfigurationStoreFactory;

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

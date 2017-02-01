package io.vertx.config.zookeeper;

import io.vertx.config.spi.ConfigStore;
import io.vertx.config.spi.ConfigStoreFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Factory to create {@link ZookeeperConfigStore} instances.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ZookeeperConfigStoreFactory implements ConfigStoreFactory {
  @Override
  public String name() {
    return "zookeeper";
  }

  @Override
  public ConfigStore create(Vertx vertx, JsonObject configuration) {
    return new ZookeeperConfigStore(vertx, configuration);
  }
}

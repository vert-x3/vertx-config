package io.vertx.config.kubernetes;

import io.vertx.config.spi.ConfigStore;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.config.spi.ConfigStoreFactory;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ConfigMapStoreFactory implements ConfigStoreFactory {
  @Override
  public String name() {
    return "configmap";
  }

  @Override
  public ConfigStore create(Vertx vertx, JsonObject configuration) {
    return new ConfigMapStore(vertx, configuration);
  }
}

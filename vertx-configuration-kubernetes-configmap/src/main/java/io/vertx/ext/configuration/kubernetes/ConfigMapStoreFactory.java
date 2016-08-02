package io.vertx.ext.configuration.kubernetes;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.configuration.spi.ConfigurationStore;
import io.vertx.ext.configuration.spi.ConfigurationStoreFactory;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ConfigMapStoreFactory implements ConfigurationStoreFactory {
  @Override
  public String name() {
    return "configmap";
  }

  @Override
  public ConfigurationStore create(Vertx vertx, JsonObject configuration) {
    return new ConfigMapStore(vertx, configuration);
  }
}

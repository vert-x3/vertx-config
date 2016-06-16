package io.vertx.ext.configuration.impl.spi;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.configuration.spi.ConfigurationStore;
import io.vertx.ext.configuration.spi.ConfigurationStoreFactory;

/**
 * The factory creating Json object configuration stores.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class JsonConfigurationStoreFactory implements ConfigurationStoreFactory {

  @Override
  public String name() {
    return "json";
  }

  @Override
  public ConfigurationStore create(Vertx vertx, JsonObject configuration) {
    return new JsonConfigurationStore(configuration);
  }
}

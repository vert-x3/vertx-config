package io.vertx.config.impl.spi;

import io.vertx.config.spi.ConfigurationStore;
import io.vertx.config.spi.ConfigurationStoreFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

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

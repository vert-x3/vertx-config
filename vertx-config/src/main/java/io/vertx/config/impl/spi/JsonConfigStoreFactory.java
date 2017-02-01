package io.vertx.config.impl.spi;

import io.vertx.config.spi.ConfigStore;
import io.vertx.config.spi.ConfigStoreFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * The factory creating Json object configuration stores.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class JsonConfigStoreFactory implements ConfigStoreFactory {

  @Override
  public String name() {
    return "json";
  }

  @Override
  public ConfigStore create(Vertx vertx, JsonObject configuration) {
    return new JsonConfigStore(configuration);
  }
}

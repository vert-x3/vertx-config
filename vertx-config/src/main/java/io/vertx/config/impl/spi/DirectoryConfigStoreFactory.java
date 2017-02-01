package io.vertx.config.impl.spi;

import io.vertx.config.spi.ConfigStore;
import io.vertx.config.spi.ConfigStoreFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * The factory to create {@link DirectoryConfigStore}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class DirectoryConfigStoreFactory implements ConfigStoreFactory {
  @Override
  public String name() {
    return "directory";
  }

  @Override
  public ConfigStore create(Vertx vertx, JsonObject configuration) {
    return new DirectoryConfigStore(vertx, configuration);
  }
}

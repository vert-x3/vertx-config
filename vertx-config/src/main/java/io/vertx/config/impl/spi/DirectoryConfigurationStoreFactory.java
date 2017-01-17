package io.vertx.config.impl.spi;

import io.vertx.config.spi.ConfigurationStore;
import io.vertx.config.spi.ConfigurationStoreFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * The factory to create {@link DirectoryConfigurationStore}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class DirectoryConfigurationStoreFactory implements ConfigurationStoreFactory {
  @Override
  public String name() {
    return "directory";
  }

  @Override
  public ConfigurationStore create(Vertx vertx, JsonObject configuration) {
    return new DirectoryConfigurationStore(vertx, configuration);
  }
}

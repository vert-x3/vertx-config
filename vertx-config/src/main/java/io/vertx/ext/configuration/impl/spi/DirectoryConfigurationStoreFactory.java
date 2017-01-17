package io.vertx.ext.configuration.impl.spi;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.configuration.spi.ConfigurationStore;
import io.vertx.ext.configuration.spi.ConfigurationStoreFactory;

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

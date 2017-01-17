package io.vertx.config.git;

import io.vertx.config.spi.ConfigurationStore;
import io.vertx.config.spi.ConfigurationStoreFactory;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class GitConfigurationStoreFactory implements ConfigurationStoreFactory {


  @Override
  public String name() {
    return "git";
  }

  @Override
  public ConfigurationStore create(Vertx vertx, JsonObject configuration) {
    return new GitConfigurationStore(vertx, configuration);
  }
}

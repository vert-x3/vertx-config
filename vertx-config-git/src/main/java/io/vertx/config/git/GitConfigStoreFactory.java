package io.vertx.config.git;

import io.vertx.config.spi.ConfigStore;
import io.vertx.config.spi.ConfigStoreFactory;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class GitConfigStoreFactory implements ConfigStoreFactory {


  @Override
  public String name() {
    return "git";
  }

  @Override
  public ConfigStore create(Vertx vertx, JsonObject configuration) {
    return new GitConfigStore(vertx, configuration);
  }
}

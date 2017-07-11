package io.vertx.config.vault;

import io.vertx.config.spi.ConfigStore;
import io.vertx.config.spi.ConfigStoreFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Implementation of {@link ConfigStoreFactory} to create {@link VaultConfigStore}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class VaultConfigStoreFactory implements ConfigStoreFactory {
  @Override
  public String name() {
    return "vault";
  }

  @Override
  public ConfigStore create(Vertx vertx, JsonObject configuration) {
    return new VaultConfigStore(vertx, configuration);
  }
}

package io.vertx.config.spring;

import io.vertx.config.spi.ConfigurationStore;
import io.vertx.config.spi.ConfigurationStoreFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Factory to create instance of {@link SpringConfigServerStore}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class SpringConfigServerStoreFactory implements ConfigurationStoreFactory {


  @Override
  public String name() {
    return "spring-config-server";
  }

  @Override
  public ConfigurationStore create(Vertx vertx, JsonObject configuration) {
    return new SpringConfigServerStore(vertx, configuration);
  }
}

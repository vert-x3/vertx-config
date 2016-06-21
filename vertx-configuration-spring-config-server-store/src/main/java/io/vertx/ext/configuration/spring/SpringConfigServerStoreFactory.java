package io.vertx.ext.configuration.spring;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.configuration.spi.ConfigurationStore;
import io.vertx.ext.configuration.spi.ConfigurationStoreFactory;

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

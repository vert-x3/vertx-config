package io.vertx.config.impl.spi;

import io.vertx.config.spi.ConfigurationStore;
import io.vertx.config.spi.ConfigurationStoreFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.Objects;

/**
 * The factory to create {@link EventBusConfigurationStore}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class EventBusConfigurationStoreFactory implements ConfigurationStoreFactory {
  @Override
  public String name() {
    return "event-bus";
  }

  @Override
  public ConfigurationStore create(Vertx vertx, JsonObject configuration) {
    String address = configuration.getString("address");
    Objects.requireNonNull(address);
    return new EventBusConfigurationStore(vertx, address);
  }
}

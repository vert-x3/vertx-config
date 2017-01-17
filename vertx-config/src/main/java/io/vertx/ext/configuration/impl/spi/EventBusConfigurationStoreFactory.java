package io.vertx.ext.configuration.impl.spi;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.configuration.spi.ConfigurationStore;
import io.vertx.ext.configuration.spi.ConfigurationStoreFactory;

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

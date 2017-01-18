package io.vertx.config.impl.spi;

import io.vertx.config.spi.ConfigStore;
import io.vertx.config.spi.ConfigStoreFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.Objects;

/**
 * The factory to create {@link EventBusConfigStore}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class EventBusConfigStoreFactory implements ConfigStoreFactory {
  @Override
  public String name() {
    return "event-bus";
  }

  @Override
  public ConfigStore create(Vertx vertx, JsonObject configuration) {
    String address = configuration.getString("address");
    Objects.requireNonNull(address);
    return new EventBusConfigStore(vertx, address);
  }
}

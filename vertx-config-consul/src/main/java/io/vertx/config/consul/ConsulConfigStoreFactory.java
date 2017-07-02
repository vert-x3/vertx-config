package io.vertx.config.consul;

import io.vertx.config.spi.ConfigStore;
import io.vertx.config.spi.ConfigStoreFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="mailto:ruslan.sennov@gmail.com">Ruslan Sennov</a>
 */
public class ConsulConfigStoreFactory implements ConfigStoreFactory {
  @Override
  public String name() {
    return "consul";
  }

  @Override
  public ConfigStore create(Vertx vertx, JsonObject configuration) {
    return new ConsulConfigStore(vertx, configuration);
  }
}

package io.vertx.config.spi;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Factory to create instances of {@link ConfigStore}. This is a SPI, and so implementations are retrieved
 * from the classpath / classloader using a {@link java.util.ServiceLoader}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public interface ConfigStoreFactory {

  /**
   * @return the name of the factory, used in the {@link io.vertx.config.ConfigurationVerticle} configuration
   * to create an instance of the store.
   */
  String name();

  /**
   * Creates an instance of the {@link ConfigStore}.
   *
   * @param vertx         the vert.x instance
   * @param configuration the configuration
   * @return the created configuration store
   */
  ConfigStore create(Vertx vertx, JsonObject configuration);

}

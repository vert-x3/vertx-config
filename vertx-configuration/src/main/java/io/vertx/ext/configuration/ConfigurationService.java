package io.vertx.ext.configuration;

import io.vertx.codegen.annotations.CacheReturn;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.configuration.impl.ConfigurationServiceImpl;

/**
 * Defines a configuration service that read configuration from {@link io.vertx.ext.configuration.spi.ConfigurationStore}
 * and tracks changes periodically.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@VertxGen
public interface ConfigurationService {

  /**
   * Creates an instance of the default implementation of the {@link ConfigurationService}.
   *
   * @param vertx   the vert.x instance
   * @param options the options, must not be {@code null}, must contain the list of configured store.
   * @return the created instance.
   */
  static ConfigurationService create(Vertx vertx, ConfigurationServiceOptions options) {
    return new ConfigurationServiceImpl(vertx, options);
  }

  static ConfigurationService create(Vertx vertx) {
    ConfigurationServiceOptions options = new ConfigurationServiceOptions();
    options
        .addStore(
            new ConfigurationStoreOptions().setType("json")
                .setConfig(vertx.getOrCreateContext().config()))
        .addStore(
            new ConfigurationStoreOptions().setType("sys")
        )
        .addStore(new ConfigurationStoreOptions().setType("env")
        );
    return create(vertx, options);
  }

  /**
   * Reads the configuration from the different {@link io.vertx.ext.configuration.spi.ConfigurationStore}
   * and computes the final configuration.
   *
   * @param completionHandler handler receiving the computed configuration, or a failure if the
   *                          configuration cannot be retrieved
   */
  void getConfiguration(Handler<AsyncResult<JsonObject>> completionHandler);

  /**
   * Same as {@link #getConfiguration(Handler)}, but returning a {@link Future} object.
   */
  @GenIgnore
  Future<JsonObject> getConfiguration();

  /**
   * Closes the service.
   */
  void close();

  /**
   * Gets the last computed configuration.
   *
   * @return the last configuration
   */
  JsonObject getCachedConfiguration();

  /**
   * Registers a listener receiving configuration changes. This method cannot only be called if
   * the configuration is broadcasted.
   *
   * @param listener the listener
   */
  void listen(Handler<ConfigurationChange> listener);

  /**
   * @return the stream of configurations.
   */
  @CacheReturn
  ConfigurationStream configurationStream();

}

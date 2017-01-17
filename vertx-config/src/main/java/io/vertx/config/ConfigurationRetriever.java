package io.vertx.config;

import io.vertx.codegen.annotations.CacheReturn;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.config.spi.ConfigurationStore;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.config.impl.ConfigurationRetrieverImpl;

/**
 * Defines a configuration retriever that read configuration from
 * {@link ConfigurationStore}
 * and tracks changes periodically.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@VertxGen
public interface ConfigurationRetriever {

  /**
   * Creates an instance of the default implementation of the {@link ConfigurationRetriever}.
   *
   * @param vertx   the vert.x instance
   * @param options the options, must not be {@code null}, must contain the list of configured store.
   * @return the created instance.
   */
  static ConfigurationRetriever create(Vertx vertx, ConfigurationRetrieverOptions options) {
    return new ConfigurationRetrieverImpl(vertx, options);
  }

  /**
   * Creates an instance of the default implementation of the {@link ConfigurationRetriever}, using the default
   * settings (json file, system properties and environment variables).
   *
   * @param vertx   the vert.x instance
   * @return the created instance.
   */
  static ConfigurationRetriever create(Vertx vertx) {
    ConfigurationRetrieverOptions options = new ConfigurationRetrieverOptions();
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
   * Reads the configuration from the different {@link ConfigurationStore}
   * and computes the final configuration.
   *
   * @param completionHandler handler receiving the computed configuration, or a failure if the
   *                          configuration cannot be retrieved
   */
  void getConfiguration(Handler<AsyncResult<JsonObject>> completionHandler);

  /**
   * Same as {@link #getConfiguration(Handler)}, but returning a {@link Future} object. The result is a
   * {@link JsonObject}.
   */
  Future<JsonObject> getConfigurationFuture();


  /**
   * Closes the retriever.
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

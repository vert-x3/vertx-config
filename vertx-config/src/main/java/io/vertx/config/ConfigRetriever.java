package io.vertx.config;

import io.vertx.codegen.annotations.CacheReturn;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.config.impl.ConfigRetrieverImpl;
import io.vertx.config.spi.ConfigStore;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;

/**
 * Defines a configuration retriever that read configuration from
 * {@link ConfigStore}
 * and tracks changes periodically.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@VertxGen
public interface ConfigRetriever {

  /**
   * Creates an instance of the default implementation of the {@link ConfigRetriever}.
   *
   * @param vertx   the vert.x instance
   * @param options the options, must not be {@code null}, must contain the list of configured store.
   * @return the created instance.
   */
  static ConfigRetriever create(Vertx vertx, ConfigRetrieverOptions options) {
    ConfigRetrieverImpl retriever = new ConfigRetrieverImpl(vertx, options);
    retriever.initializePeriodicScan();
    return retriever;
  }

  /**
   * Creates an instance of the default implementation of the {@link ConfigRetriever}, using the default
   * settings (json file, system properties and environment variables).
   *
   * @param vertx the vert.x instance
   * @return the created instance.
   */
  static ConfigRetriever create(Vertx vertx) {
    ConfigRetrieverOptions options = new ConfigRetrieverOptions();
    options
      .addStore(
        new ConfigStoreOptions().setType("json")
          .setConfig(vertx.getOrCreateContext().config()))
      .addStore(
        new ConfigStoreOptions().setType("sys")
      )
      .addStore(new ConfigStoreOptions().setType("env")
      );
    return create(vertx, options);
  }

  /**
   * Same as {@link ConfigRetriever#getConfig(Handler)}, but returning a {@link Future} object. The result is a
   * {@link JsonObject}.
   *
   * @param retriever the config retrieve
   * @return the future completed when the configuration is retrieved
   */
  static Future<JsonObject> getConfigAsFuture(ConfigRetriever retriever) {
    Future<JsonObject> future = Future.future();
    retriever.getConfig(future.completer());
    return future;
  }

  /**
   * Reads the configuration from the different {@link ConfigStore}
   * and computes the final configuration.
   *
   * @param completionHandler handler receiving the computed configuration, or a failure if the
   *                          configuration cannot be retrieved
   */
  void getConfig(Handler<AsyncResult<JsonObject>> completionHandler);


  /**
   * Closes the retriever.
   */
  void close();

  /**
   * Gets the last computed configuration.
   *
   * @return the last configuration
   */
  JsonObject getCachedConfig();

  /**
   * Registers a listener receiving configuration changes. This method cannot only be called if
   * the configuration is broadcasted.
   *
   * @param listener the listener
   */
  void listen(Handler<ConfigChange> listener);

  /**
   * @return the stream of configurations. It's single stream (unicast) and that delivers the last known config
   * and the successors periodically.
   */
  @CacheReturn
  ReadStream<JsonObject> configStream();

}

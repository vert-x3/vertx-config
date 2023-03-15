/*
 * Copyright (c) 2014 Red Hat, Inc. and others
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package io.vertx.config;

import io.vertx.codegen.annotations.CacheReturn;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.config.impl.ConfigRetrieverImpl;
import io.vertx.config.spi.ConfigStore;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;

import java.util.function.Function;

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
    return create(vertx, new ConfigRetrieverOptions().setIncludeDefaultStores(true));
  }

  /**
   * Reads the configuration from the different {@link ConfigStore}
   * and computes the final configuration.
   *
   * @return a future notified the computed configuration, or a failure if the
   *                          configuration cannot be retrieved
   */
  Future<JsonObject> getConfig();

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
   * Registers a handler called before every scan. This method is mostly used for logging purpose.
   * @param handler the handler, must not be {@code null}
   * @return the current config retriever
   */
  @Fluent
  ConfigRetriever setBeforeScanHandler(Handler<Void> handler);

  /**
   * Registers a handler that process the configuration before being injected into {@link #getConfig(Handler)} or {@link #listen(Handler)}. This allows
   * the code to customize the configuration.
   *
   * @param processor the processor, must not be {@code null}. The method must not return {@code null}. The returned configuration is used. If the processor
   *                  does not update the configuration, it must return the input configuration. If the processor throws an exception, the failure is passed
   *                  to the {@link #getConfig(Handler)} handler.
   * @return the current config retriever
   */
  @Fluent
  ConfigRetriever setConfigurationProcessor(Function<JsonObject, JsonObject> processor);

  /**
   * @return the stream of configurations. It's single stream (unicast) and that delivers the last known config
   * and the successors periodically.
   */
  @CacheReturn
  ReadStream<JsonObject> configStream();

}

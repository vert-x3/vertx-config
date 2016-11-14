/*
 * Copyright 2014 Red Hat, Inc.
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
 */

package io.vertx.rxjava.ext.configuration;

import java.util.Map;
import rx.Observable;
import io.vertx.ext.configuration.ConfigurationServiceOptions;
import io.vertx.rxjava.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.rxjava.core.Future;
import io.vertx.ext.configuration.ConfigurationChange;

/**
 * Defines a configuration service that read configuration from {@link io.vertx.rxjava.ext.configuration.spi.ConfigurationStore}
 * and tracks changes periodically.
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.configuration.ConfigurationService original} non RX-ified interface using Vert.x codegen.
 */

public class ConfigurationService {

  final io.vertx.ext.configuration.ConfigurationService delegate;

  public ConfigurationService(io.vertx.ext.configuration.ConfigurationService delegate) {
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  /**
   * Creates an instance of the default implementation of the {@link io.vertx.rxjava.ext.configuration.ConfigurationService}.
   * @param vertx the vert.x instance
   * @param options the options, must not be <code>null</code>, must contain the list of configured store.
   * @return the created instance.
   */
  public static ConfigurationService create(Vertx vertx, ConfigurationServiceOptions options) { 
    ConfigurationService ret = ConfigurationService.newInstance(io.vertx.ext.configuration.ConfigurationService.create((io.vertx.core.Vertx)vertx.getDelegate(), options));
    return ret;
  }

  public static ConfigurationService create(Vertx vertx) { 
    ConfigurationService ret = ConfigurationService.newInstance(io.vertx.ext.configuration.ConfigurationService.create((io.vertx.core.Vertx)vertx.getDelegate()));
    return ret;
  }

  /**
   * Reads the configuration from the different {@link io.vertx.rxjava.ext.configuration.spi.ConfigurationStore}
   * and computes the final configuration.
   * @param completionHandler handler receiving the computed configuration, or a failure if the configuration cannot be retrieved
   */
  public void getConfiguration(Handler<AsyncResult<JsonObject>> completionHandler) { 
    delegate.getConfiguration(completionHandler);
  }

  /**
   * Reads the configuration from the different {@link io.vertx.rxjava.ext.configuration.spi.ConfigurationStore}
   * and computes the final configuration.
   * @return 
   */
  public Observable<JsonObject> getConfigurationObservable() { 
    io.vertx.rx.java.ObservableFuture<JsonObject> completionHandler = io.vertx.rx.java.RxHelper.observableFuture();
    getConfiguration(completionHandler.toHandler());
    return completionHandler;
  }

  /**
   * Same as {@link io.vertx.rxjava.ext.configuration.ConfigurationService#getConfiguration}, but returning a  object. The result is a
   * . In Java, you can use {@link io.vertx.rxjava.ext.configuration.ConfigurationService#getConfiguration}.
   * @return 
   */
  public <T> Future<T> getConfigurationFuture() { 
    Future<T> ret = Future.newInstance(delegate.getConfigurationFuture());
    return ret;
  }

  /**
   * Closes the service.
   */
  public void close() { 
    delegate.close();
  }

  /**
   * Gets the last computed configuration.
   * @return the last configuration
   */
  public JsonObject getCachedConfiguration() { 
    JsonObject ret = delegate.getCachedConfiguration();
    return ret;
  }

  /**
   * Registers a listener receiving configuration changes. This method cannot only be called if
   * the configuration is broadcasted.
   * @param listener the listener
   */
  public void listen(Handler<ConfigurationChange> listener) { 
    delegate.listen(listener);
  }

  /**
   * @return the stream of configurations.
   * @return 
   */
  public ConfigurationStream configurationStream() { 
    if (cached_0 != null) {
      return cached_0;
    }
    ConfigurationStream ret = ConfigurationStream.newInstance(delegate.configurationStream());
    cached_0 = ret;
    return ret;
  }

  private ConfigurationStream cached_0;

  public static ConfigurationService newInstance(io.vertx.ext.configuration.ConfigurationService arg) {
    return arg != null ? new ConfigurationService(arg) : null;
  }
}

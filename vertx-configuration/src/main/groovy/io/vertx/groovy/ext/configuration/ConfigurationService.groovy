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

package io.vertx.groovy.ext.configuration;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.core.json.JsonObject
import io.vertx.ext.configuration.ConfigurationServiceOptions
import io.vertx.groovy.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.groovy.core.Future
import io.vertx.ext.configuration.ConfigurationChange
/**
 * Defines a configuration service that read configuration from {@link io.vertx.groovy.ext.configuration.spi.ConfigurationStore}
 * and tracks changes periodically.
*/
@CompileStatic
public class ConfigurationService {
  private final def io.vertx.ext.configuration.ConfigurationService delegate;
  public ConfigurationService(Object delegate) {
    this.delegate = (io.vertx.ext.configuration.ConfigurationService) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  /**
   * Creates an instance of the default implementation of the {@link io.vertx.groovy.ext.configuration.ConfigurationService}.
   * @param vertx the vert.x instance
   * @param options the options, must not be <code>null</code>, must contain the list of configured store. (see <a href="../../../../../../../cheatsheet/ConfigurationServiceOptions.html">ConfigurationServiceOptions</a>)
   * @return the created instance.
   */
  public static ConfigurationService create(Vertx vertx, Map<String, Object> options) {
    def ret = InternalHelper.safeCreate(io.vertx.ext.configuration.ConfigurationService.create(vertx != null ? (io.vertx.core.Vertx)vertx.getDelegate() : null, options != null ? new io.vertx.ext.configuration.ConfigurationServiceOptions(io.vertx.lang.groovy.InternalHelper.toJsonObject(options)) : null), io.vertx.groovy.ext.configuration.ConfigurationService.class);
    return ret;
  }
  public static ConfigurationService create(Vertx vertx) {
    def ret = InternalHelper.safeCreate(io.vertx.ext.configuration.ConfigurationService.create(vertx != null ? (io.vertx.core.Vertx)vertx.getDelegate() : null), io.vertx.groovy.ext.configuration.ConfigurationService.class);
    return ret;
  }
  /**
   * Reads the configuration from the different {@link io.vertx.groovy.ext.configuration.spi.ConfigurationStore}
   * and computes the final configuration.
   * @param completionHandler handler receiving the computed configuration, or a failure if the configuration cannot be retrieved
   */
  public void getConfiguration(Handler<AsyncResult<Map<String, Object>>> completionHandler) {
    delegate.getConfiguration(completionHandler != null ? new Handler<AsyncResult<io.vertx.core.json.JsonObject>>() {
      public void handle(AsyncResult<io.vertx.core.json.JsonObject> ar) {
        if (ar.succeeded()) {
          completionHandler.handle(io.vertx.core.Future.succeededFuture((Map<String, Object>)InternalHelper.wrapObject(ar.result())));
        } else {
          completionHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    } : null);
  }
  /**
   * Same as {@link io.vertx.groovy.ext.configuration.ConfigurationService#getConfiguration}, but returning a  object. The result is a
   * . In Java, you can use {@link io.vertx.groovy.ext.configuration.ConfigurationService#getConfiguration}.
   * @return 
   */
  public <T> Future<T> getConfigurationFuture() {
    def ret = InternalHelper.safeCreate(delegate.getConfigurationFuture(), io.vertx.groovy.core.Future.class);
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
  public Map<String, Object> getCachedConfiguration() {
    def ret = (Map<String, Object>)InternalHelper.wrapObject(delegate.getCachedConfiguration());
    return ret;
  }
  /**
   * Registers a listener receiving configuration changes. This method cannot only be called if
   * the configuration is broadcasted.
   * @param listener the listener
   */
  public void listen(Handler<Map<String, Object>> listener) {
    delegate.listen(listener != null ? new Handler<io.vertx.ext.configuration.ConfigurationChange>(){
      public void handle(io.vertx.ext.configuration.ConfigurationChange event) {
        listener.handle((Map<String, Object>)InternalHelper.wrapObject(event?.toJson()));
      }
    } : null);
  }
  /**
   * @return the stream of configurations.
   * @return 
   */
  public ConfigurationStream configurationStream() {
    if (cached_0 != null) {
      return cached_0;
    }
    def ret = InternalHelper.safeCreate(delegate.configurationStream(), io.vertx.groovy.ext.configuration.ConfigurationStream.class);
    cached_0 = ret;
    return ret;
  }
  private ConfigurationStream cached_0;
}

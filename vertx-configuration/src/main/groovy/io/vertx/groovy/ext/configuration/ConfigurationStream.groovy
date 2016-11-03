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
import io.vertx.groovy.core.streams.ReadStream
import io.vertx.core.json.JsonObject
import io.vertx.core.Handler
/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
*/
@CompileStatic
public class ConfigurationStream implements ReadStream<JsonObject> {
  private final def io.vertx.ext.configuration.ConfigurationStream delegate;
  public ConfigurationStream(Object delegate) {
    this.delegate = (io.vertx.ext.configuration.ConfigurationStream) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public ReadStream<JsonObject> pause() {
    ((io.vertx.core.streams.ReadStream) delegate).pause();
    return this;
  }
  public ReadStream<JsonObject> resume() {
    ((io.vertx.core.streams.ReadStream) delegate).resume();
    return this;
  }
  public ConfigurationStream exceptionHandler(Handler<Throwable> handler) {
    ((io.vertx.core.streams.StreamBase) delegate).exceptionHandler(handler);
    return this;
  }
  public ConfigurationStream handler(Handler<Map<String, Object>> handler) {
    ((io.vertx.core.streams.ReadStream) delegate).handler(handler != null ? new Handler<io.vertx.core.json.JsonObject>(){
      public void handle(io.vertx.core.json.JsonObject event) {
        handler.handle((Map<String, Object>)InternalHelper.wrapObject(event));
      }
    } : null);
    return this;
  }
  public ConfigurationStream endHandler(Handler<Void> endHandler) {
    ((io.vertx.core.streams.ReadStream) delegate).endHandler(endHandler);
    return this;
  }
}

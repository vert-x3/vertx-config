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
import io.vertx.rxjava.core.streams.ReadStream;
import io.vertx.core.json.JsonObject;
import io.vertx.core.Handler;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link io.vertx.ext.configuration.ConfigurationStream original} non RX-ified interface using Vert.x codegen.
 */

public class ConfigurationStream implements ReadStream<JsonObject> {

  final io.vertx.ext.configuration.ConfigurationStream delegate;

  public ConfigurationStream(io.vertx.ext.configuration.ConfigurationStream delegate) {
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  private rx.Observable<JsonObject> observable;

  public synchronized rx.Observable<JsonObject> toObservable() {
    if (observable == null) {
      observable = io.vertx.rx.java.RxHelper.toObservable((io.vertx.core.streams.ReadStream<io.vertx.core.json.JsonObject>) this.getDelegate());
    }
    return observable;
  }

  public ReadStream<JsonObject> pause() { 
    delegate.pause();
    return this;
  }

  public ReadStream<JsonObject> resume() { 
    delegate.resume();
    return this;
  }

  public ConfigurationStream exceptionHandler(Handler<Throwable> handler) { 
    ((io.vertx.core.streams.StreamBase) delegate).exceptionHandler(handler);
    return this;
  }

  public ConfigurationStream handler(Handler<JsonObject> handler) { 
    ((io.vertx.core.streams.ReadStream) delegate).handler(handler);
    return this;
  }

  public ConfigurationStream endHandler(Handler<Void> endHandler) { 
    ((io.vertx.core.streams.ReadStream) delegate).endHandler(endHandler);
    return this;
  }


  public static ConfigurationStream newInstance(io.vertx.ext.configuration.ConfigurationStream arg) {
    return arg != null ? new ConfigurationStream(arg) : null;
  }
}

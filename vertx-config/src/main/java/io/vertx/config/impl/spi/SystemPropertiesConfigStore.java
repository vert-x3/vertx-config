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

package io.vertx.config.impl.spi;

import io.vertx.config.spi.ConfigStore;
import io.vertx.config.spi.ConfigStoreFactory;
import io.vertx.config.spi.utils.JsonObjectHelper;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

/**
 * An implementation of configuration store loading the content from the system properties.
 * <p>
 * As this configuration store is a singleton, the factory returns always the same instance.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class SystemPropertiesConfigStore implements ConfigStore, ConfigStoreFactory {
  private boolean cache;
  private JsonObject cached;
  private Boolean rawData;

  @Override
  public String name() {
    return "sys";
  }

  @Override
  public ConfigStore create(Vertx vertx, JsonObject configuration) {
    cache = configuration.getBoolean("cache", true);
    rawData = configuration.getBoolean("raw-data", false);
    return this;
  }

  @Override
  public void get(Handler<AsyncResult<Buffer>> completionHandler) {
    if (!cache || cached == null) {
      cached = JsonObjectHelper.from(System.getProperties(), rawData);
    }
    completionHandler.handle(Future.succeededFuture(Buffer.buffer(cached.encode())));
  }


}

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
import io.vertx.config.spi.utils.JsonObjectHelper;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.config.spi.ConfigStoreFactory;

import java.util.*;

/**
 * An implementation of configuration store loading the content from the environment variables.
 * <p>
 * As this configuration store is a singleton, the factory returns always the same instance.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class EnvVariablesConfigStore implements ConfigStoreFactory, ConfigStore {
  private final boolean rawData;
  private final Set<String> keys;

  private JsonObject cached;

  public EnvVariablesConfigStore() {
    this(false, null);
  }

  public EnvVariablesConfigStore(boolean rawData, JsonArray keys) {
    this.rawData = rawData;
    this.keys = (keys == null) ? null : new HashSet<>(keys.getList());
  }

  @Override
  public String name() {
    return "env";
  }

  @Override
  public ConfigStore create(Vertx vertx, JsonObject configuration) {
    return new EnvVariablesConfigStore(configuration.getBoolean("raw-data", false), configuration.getJsonArray("keys"));
  }

  @Override
  public void get(Handler<AsyncResult<Buffer>> completionHandler) {
    if (cached == null) {
      cached = all(System.getenv(), rawData, keys);
    }
    completionHandler.handle(Future.succeededFuture(Buffer.buffer(cached.encode())));
  }

  private static JsonObject all(Map<String, String> env, boolean rawData, Set<String> keys) {
    JsonObject json = new JsonObject();
    Collection localKeys = keys == null ? env.keySet() : keys;
    env.forEach((key, value) -> {
      if (localKeys.contains(key)) {
        JsonObjectHelper.put(json, key, value, rawData);
      }
    });
    return json;
  }

}

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
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * An implementation of configuration store loading the content from the environment variables.
 * <p>
 * As this configuration store is a singleton, the factory returns always the same instance.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class EnvVariablesConfigStore implements ConfigStore {

  private final VertxInternal vertx;
  private final boolean rawData;
  private final boolean hierarchical;
  private final Set<String> keys;
  private final Supplier<Map<String, String>> getenv;
  private final AtomicReference<Buffer> cached = new AtomicReference<>();

  public EnvVariablesConfigStore(Vertx vertx, boolean rawData, boolean hierarchical, JsonArray keys, Supplier<Map<String, String>> getenv) {
    this.vertx = (VertxInternal) vertx;
    this.rawData = rawData;
    this.hierarchical = hierarchical;
    this.keys = (keys == null) ? null : new HashSet<>(keys.getList());
    this.getenv = getenv;
  }

  @Override
  public Future<Buffer> get() {
    Buffer value = cached.get();
    if (value == null) {
      value = all(getenv.get(), rawData, hierarchical, keys).toBuffer();
      cached.set(value);
    }
    return vertx.getOrCreateContext().succeededFuture(value);
  }

  private static JsonObject all(Map<String, String> env, boolean rawData, boolean hierarchical, Set<String> keys) {
    JsonObject json = new JsonObject();
    for (Map.Entry<String, String> entry : env.entrySet()) {
      if (keys == null || keys.contains(entry.getKey())) {
        JsonObjectHelper.put(json, entry.getKey(), entry.getValue(), rawData, hierarchical);
      }
    }
    return json;
  }

  @Override
  public Future<Void> close() {
    return vertx.getOrCreateContext().succeededFuture();
  }
}

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

package io.vertx.config.redis;

import io.vertx.config.spi.ConfigStore;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.config.spi.ConfigStoreFactory;

/**
 * Factory to create {@link RedisConfigStore} instances.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class RedisConfigStoreFactory implements ConfigStoreFactory {
  @Override
  public String name() {
    return "redis";
  }

  @Override
  public ConfigStore create(Vertx vertx, JsonObject configuration) {
    return new RedisConfigStore(vertx, configuration);
  }
}

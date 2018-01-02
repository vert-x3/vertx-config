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
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;

/**
 * An implementation of configuration store reading hash from Redis.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class RedisConfigStore implements ConfigStore {

  private final RedisClient redis;
  private final String field;

  public RedisConfigStore(Vertx vertx, JsonObject config) {
    this.field = config.getString("key", "configuration");
    this.redis = RedisClient.create(vertx, new RedisOptions(config));
  }

  @Override
  public void close(Handler<Void> completionHandler) {
    redis.close(ar -> completionHandler.handle(null));
  }

  @Override
  public void get(Handler<AsyncResult<Buffer>> completionHandler) {
    redis.hgetall(field, ar -> {
      if (ar.failed()) {
        completionHandler.handle(Future.failedFuture(ar.cause()));
      } else {
        completionHandler.handle(ar.map(json -> Buffer.buffer(ar.result().encode())));
      }
    });
  }
}

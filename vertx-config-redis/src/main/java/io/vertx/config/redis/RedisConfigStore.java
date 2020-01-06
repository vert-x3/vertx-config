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
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.*;

import java.util.Iterator;

/**
 * An implementation of configuration store reading hash from Redis.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class RedisConfigStore implements ConfigStore {

  private final VertxInternal vertx;
  private final Redis redis;
  private final String field;

  private boolean closed;

  public RedisConfigStore(Vertx vertx, JsonObject config) {
    this.vertx = (VertxInternal) vertx;
    this.field = config.getString("key", "configuration");
    this.redis = Redis.createClient(vertx, new RedisOptions(config));
  }

  @Override
  public Future<Void> close() {
    redis.close();
    return vertx.getOrCreateContext().succeededFuture();
  }

  @Override
  public Future<Buffer> get() {
    return redis.send(Request.cmd(Command.HGETALL).arg(field))
      .map(resp -> {
        JsonObject result = new JsonObject();
        Iterator<Response> it = resp.iterator();
        while (it.hasNext()) {
          String key = it.next().toString();
          String value = it.next().toString();
          result.put(key, value);
        }
        return result.toBuffer();
      });
  }
}

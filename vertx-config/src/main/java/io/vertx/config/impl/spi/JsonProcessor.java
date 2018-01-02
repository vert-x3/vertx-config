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

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.config.spi.ConfigProcessor;

/**
 * Builds a json object from the given buffer.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class JsonProcessor implements ConfigProcessor {

  @Override
  public void process(Vertx vertx, JsonObject configuration, Buffer input, Handler<AsyncResult<JsonObject>> handler) {
    try {
      JsonObject json = input.toJsonObject();
      if (json == null) {
        json = new JsonObject();
      }
      handler.handle(Future.succeededFuture(json));
    } catch (Exception e) {
      handler.handle(Future.failedFuture(e));
    }
  }

  @Override
  public String name() {
    return "json";
  }
}

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

import io.vertx.config.spi.ConfigProcessor;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.json.JsonObject;

/**
 * Builds a json object from the given buffer.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class JsonProcessor implements ConfigProcessor {

  @Override
  public Future<JsonObject> process(Vertx vertx, JsonObject configuration, Buffer input) {
    Promise<JsonObject> promise = ((VertxInternal) vertx).promise();
    try {
      JsonObject json = input.toJsonObject();
      if (json == null) {
        json = new JsonObject();
      }
      promise.complete(json);
    } catch (Exception e) {
      promise.fail(e);
    }
    return promise.future();
  }

  @Override
  public String name() {
    return "json";
  }
}

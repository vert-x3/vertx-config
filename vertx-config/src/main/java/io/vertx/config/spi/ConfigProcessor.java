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

package io.vertx.config.spi;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

/**
 * A processor transforms a chunk of configuration retrieved from a configuration store as a {@link Buffer} to a
 * {@link JsonObject}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public interface ConfigProcessor {

  /**
   * Name of the processor, generally the name of the format it handles.
   *
   * @return the name
   */
  String name();

  /**
   * Transforms the given {@code input} into a {@link JsonObject}. This is an asynchronous non-blocking
   * transformation. The result is passed to the given {@code Handler}. If the transformation fails, the passed
   * {@link AsyncResult} would be marked as failed. On success, the result contains the {@link JsonObject}.
   *
   * @param vertx         the Vert.x instance
   * @param configuration the processor configuration, may be {@code null}
   * @param input         the input, must not be {@code null}
   * @param handler       the result handler, must not be {@code null}. The handler will be called in the same context as
   *                      the caller.
   */
  void process(Vertx vertx, JsonObject configuration, Buffer input, Handler<AsyncResult<JsonObject>> handler);

}

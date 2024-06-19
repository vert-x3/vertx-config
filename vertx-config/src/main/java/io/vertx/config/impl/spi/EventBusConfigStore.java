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
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.json.JsonObject;

import java.util.concurrent.atomic.AtomicReference;

/**
 * An implementation of configuration store that receive the configuration from the event bus. It
 * listens on a given address and returns the last received configuration to the next enquiry.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class EventBusConfigStore implements ConfigStore {

  private final VertxInternal vertx;
  private final MessageConsumer<Object> consumer;
  private final AtomicReference<Buffer> last = new AtomicReference<>();

  public EventBusConfigStore(Vertx vertx, String address) {
    this.vertx = (VertxInternal) vertx;
    consumer = vertx.eventBus().consumer(address);
    consumer.handler(message -> {
      Object body = message.body();
      if (body instanceof JsonObject) {
        last.set(((JsonObject) body).toBuffer());
      } else if (body instanceof Buffer) {
        last.set((Buffer) body);
      }
    });
  }

  @Override
  public Future<Void> close() {
    return consumer.unregister();
  }

  @Override
  public Future<Buffer> get() {
    Buffer buffer = last.get();
    ContextInternal context = vertx.getOrCreateContext();
    return context.succeededFuture(buffer != null ? buffer : Buffer.buffer("{}"));
  }
}

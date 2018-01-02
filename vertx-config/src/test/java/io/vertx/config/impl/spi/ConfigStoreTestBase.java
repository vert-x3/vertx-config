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

import io.vertx.config.ConfigRetriever;
import io.vertx.config.spi.ConfigStore;
import io.vertx.config.spi.ConfigStoreFactory;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.config.spi.ConfigProcessor;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public abstract class ConfigStoreTestBase {

  public static final ConfigProcessor JSON = new JsonProcessor();
  public static final ConfigProcessor PROPERTIES = new PropertiesConfigProcessor();

  protected Vertx vertx;
  protected ConfigStoreFactory factory;
  protected ConfigStore store;
  protected ConfigRetriever retriever;

  @Before
  public void setUp(TestContext context) {
    vertx = Vertx.vertx();
    vertx.exceptionHandler(context.exceptionHandler());
  }

  @After
  public void tearDown() {
    AtomicBoolean done = new AtomicBoolean();
    if (store != null) {
      store.close(v -> done.set(true));
      await().untilAtomic(done, is(true));
      done.set(false);
    }

    if (retriever != null) {
      retriever.close();
    }

    vertx.close(v -> done.set(true));

    await().untilAtomic(done, is(true));
  }

  protected void getJsonConfiguration(Vertx vertx, ConfigStore store, Handler<AsyncResult<JsonObject>> handler) {
    store.get(buffer -> {
      if (buffer.failed()) {
        handler.handle(Future.failedFuture(buffer.cause()));
      } else {
        JSON.process(vertx, new JsonObject(), buffer.result(), handler);
      }
    });
  }

  protected void getPropertiesConfiguration(Vertx vertx, ConfigStore store, Handler<AsyncResult<JsonObject>> handler) {
    store.get(buffer -> {
      if (buffer.failed()) {
        handler.handle(Future.failedFuture(buffer.cause()));
      } else {
        PROPERTIES.process(vertx, null, buffer.result(), handler);
      }
    });
  }
}

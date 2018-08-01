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

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class EventBusConfigStoreTest extends ConfigStoreTestBase {

  @Before
  public void init() {
    factory = new EventBusConfigStoreFactory();
  }

  @Test
  public void testWithSend(TestContext tc) {
    testEventBusConfigStore(tc, new JsonObject(HttpConfigStoreTest.JSON), true);
  }

  @Test
  public void testWithSendWithBuffer(TestContext tc) {
    testEventBusConfigStore(tc, Buffer.buffer(HttpConfigStoreTest.JSON), true);
  }

  @Test
  public void testWithPublish(TestContext tc) {
    testEventBusConfigStore(tc, new JsonObject(HttpConfigStoreTest.JSON), false);
  }

  @Test
  public void testWithPublishWithBuffer(TestContext tc) {
    testEventBusConfigStore(tc, Buffer.buffer(HttpConfigStoreTest.JSON), false);
  }

  private void testEventBusConfigStore(TestContext tc, Object config, boolean send) {
    store = factory.create(vertx, new JsonObject().put("address", "config"));
    Async async = tc.async(2);
    getJsonConfiguration(vertx, store, ar -> {
      assertThat(ar.result().isEmpty()).isTrue();
      async.countDown();
      if (send) {
        vertx.eventBus().send("config", config);
      } else {
        vertx.eventBus().publish("config", config);
      }
      vertx.setTimer(10, tid -> getConfigAndCheck(tc, async));
    });
  }

  private void getConfigAndCheck(TestContext tc, Async async) {
    getJsonConfiguration(vertx, store, tc.asyncAssertSuccess(res -> {
      if (res.isEmpty()) {
        // Retry as the publication may not have been dispatched yet.
        vertx.setTimer(10, tid -> getConfigAndCheck(tc, async));
      } else {
        ConfigChecker.check(res);
        async.countDown();
      }
    }));
  }
}

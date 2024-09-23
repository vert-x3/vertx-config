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

package io.vertx.config.tests.spi;

import io.vertx.config.impl.spi.EnvVariablesConfigStoreFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class EnvVariablesConfigStoreTest extends ConfigStoreTestBase {

  @Before
  public void init() {
    factory = new EnvVariablesConfigStoreFactory();
    store = factory.create(vertx, new JsonObject());
  }

  @Test
  public void testName() {
    assertThat(factory.name()).isNotNull().isEqualTo("env");
  }

  @Test
  public void testLoadingFromEnv(TestContext context) {
    Async async = context.async();
    getJsonConfiguration(vertx, store, ar -> {
      assertThat(ar.succeeded()).isTrue();
      assertThat(ar.result().getString("PATH")).isNotNull();
      async.complete();
    });
  }

  @Test
  public void testLoadingFromEnvWithNullKeySet(TestContext context) {
    Async async = context.async();
    store = factory.create(vertx, new JsonObject().put("keys", (JsonArray) null));
    getJsonConfiguration(vertx, store, ar -> {
      assertThat(ar.succeeded()).isTrue();
      assertThat(ar.result().getString("PATH")).isNotNull();
      async.complete();
    });
  }

  @Test(expected = ClassCastException.class)
  public void testLoadingFromEnvWithInvalidKeySet() {
    store = factory.create(vertx, new JsonObject().put("keys", "invalid"));
  }

  /**
   * Reproducer for https://github.com/vert-x3/vertx-config/issues/31.
   */
  @Test
  public void testLoadingFromEnvWithKeySet(TestContext context) {
    Async async = context.async();
    store = factory.create(vertx, new JsonObject().put("keys", new JsonArray().add("USER").add("HOME")));
    getJsonConfiguration(vertx, store, ar -> {
      assertThat(ar.succeeded()).isTrue();
      assertThat(ar.result().getString("PATH")).isNull();
      assertThat(ar.result().getString("USER")).isNotNull();
      assertThat(ar.result().getString("HOME")).isNotNull();
      async.complete();
    });
  }
}

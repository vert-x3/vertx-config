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

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class SystemPropertiesConfigStoreTest extends ConfigStoreTestBase {

  @Before
  public void init() {
    factory = new SystemPropertiesConfigStoreFactory();
    System.setProperty("key", "value");
    System.setProperty("sub", "{\"foo\":\"bar\"}");
    System.setProperty("array", "[1, 2, 3]");
    System.setProperty("int", "5");
    System.setProperty("float", "25.3");
    System.setProperty("true", "true");
    System.setProperty("false", "false");

    System.setProperty("hierarchical.key", "value");
    System.setProperty("hierarchical.sub.foo", "bar");
    System.setProperty("hierarchical.array", "[1, 2, 3]");
    System.setProperty("hierarchical.int", "5");
    System.setProperty("hierarchical.float", "25.3");
    System.setProperty("hierarchical.true", "true");
    System.setProperty("hierarchical.false", "false");
  }

  @After
  public void cleanup() {
    System.clearProperty("key");
    System.clearProperty("sub");
    System.clearProperty("array");
    System.clearProperty("int");
    System.clearProperty("float");
    System.clearProperty("true");
    System.clearProperty("false");

    System.clearProperty("new");
  }

  @Test
  public void testName() {
    assertThat(factory.name()).isNotNull().isEqualTo("sys");
  }

  @Test
  public void testLoadingFromSystemProperties(TestContext context) {
    Async async = context.async();
    store = factory.create(vertx, new JsonObject());
    getJsonConfiguration(vertx, store, ar -> {
      ConfigChecker.check(ar);

      // Check that properties were loaded to a flat object (hierarchical = false)
      // At this point we know that the ar is successful (otherwise ConfigChecker.check would have failed)
      JsonObject json = ar.result();
      assertThat(json.getString("hierarchical.key")).isEqualTo("value");
      assertThat(json.getString("hierarchical.sub.foo")).isEqualTo("bar");
      assertThat(json.getJsonArray("hierarchical.array")).containsExactly(1, 2, 3);
      assertThat(json.getInteger("hierarchical.int")).isEqualTo(5);
      assertThat(json.getDouble("hierarchical.float")).isEqualTo(25.3);
      assertThat(json.getBoolean("true")).isTrue();
      assertThat(json.getBoolean("false")).isFalse();

      // By default, the configuration is cached, try adding some entries
      System.setProperty("new", "some new value");
      getJsonConfiguration(vertx, store, ar2 -> {
        ConfigChecker.check(ar2);
        assertThat(ar2.result().getString("new")).isNull();
        async.complete();
      });
    });
  }

  @Test
  public void testHierarchicalLoadingFromSystemProperties(TestContext context) {
    Async async = context.async();
    store = factory.create(vertx, new JsonObject().put("hierarchical", true));
    getJsonConfiguration(vertx, store, ar -> {
      // Check that the flat keys are still present
      ConfigChecker.check(ar);

      // Check that the "hierarchical" key was mapped to a deep JSON object
      ConfigChecker.check(ar.map(json -> json.getJsonObject("hierarchical", new JsonObject())));
      async.complete();
    });
  }

  @Test
  public void testLoadingFromSystemPropertiesWithoutCache(TestContext context) {
    Async async = context.async();
    store = factory.create(vertx, new JsonObject().put("cache", false));
    getJsonConfiguration(vertx, store, ar -> {
      ConfigChecker.check(ar);
      System.setProperty("new", "some new value");
      getJsonConfiguration(vertx, store, ar2 -> {
        ConfigChecker.check(ar2);
        assertThat(ar2.result().getString("new")).isEqualToIgnoringCase("some new value");
        async.complete();
      });
    });
  }

}

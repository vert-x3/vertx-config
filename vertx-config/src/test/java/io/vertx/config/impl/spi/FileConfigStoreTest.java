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
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class FileConfigStoreTest extends ConfigStoreTestBase {

  @Before
  public void init() {
    factory = new FileConfigtoreFactory();
  }


  @Test(expected = IllegalArgumentException.class)
  public void testWithMissingPathInConf() {
    store = factory.create(vertx, new JsonObject().put("no-path", ""));
  }

  @Test
  public void testName() {
    assertThat(factory.name()).isNotNull().isEqualTo("file");
  }


  @Test
  public void testLoadingFromRegularJsonFile(TestContext context) {
    Async async = context.async();
    store = factory.create(vertx, new JsonObject().put("path", "src/test/resources/file/regular.json"));
    getJsonConfiguration(vertx, store, ar -> {
      ConfigChecker.check(ar);
      async.complete();
    });
  }

  @Test
  public void testLoadingFromRegularPropertiesFile(TestContext context) {
    Async async = context.async();
    store = factory.create(vertx, new JsonObject().put("path", "src/test/resources/file/regular.properties"));
    getPropertiesConfiguration(vertx, store, ar -> {
      assertThat(ar.succeeded()).isTrue();
      JsonObject json = ar.result();
      assertThat(json.getString("key")).isEqualTo("value");
      assertThat(json.getBoolean("true")).isTrue();
      assertThat(json.getBoolean("false")).isFalse();
      assertThat(json.getString("missing")).isNull();
      assertThat(json.getInteger("int")).isEqualTo(5);
      assertThat(json.getDouble("float")).isEqualTo(25.3);
      async.complete();
    });
  }

  @Test
  public void testLoadingFromMissingFile(TestContext context) {
    Async async = context.async();
    store = factory.create(vertx, new JsonObject().put("path", "src/test/resources/file/missing.json"));
    getJsonConfiguration(vertx, store, ar -> {
      assertThat(ar.failed()).isTrue();
      async.complete();
    });
  }

  @Test
  public void testLoadingFromANonJsonFile(TestContext context) {
    Async async = context.async();
    store = factory.create(vertx, new JsonObject().put("path", "src/test/resources/file/some-text.txt"));
    getJsonConfiguration(vertx, store, ar -> {
      assertThat(ar.failed()).isTrue();
      async.complete();
    });
  }

  @Test
  public void testLoadingFromAJsonArrayFile(TestContext context) {
    Async async = context.async();
    store = factory.create(vertx, new JsonObject().put("path", "src/test/resources/file/array.json"));
    getJsonConfiguration(vertx, store, ar -> {
      assertThat(ar.failed()).isTrue();
      async.complete();
    });
  }

}

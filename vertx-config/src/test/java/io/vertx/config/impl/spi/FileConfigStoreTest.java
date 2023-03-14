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
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
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
    factory = new FileConfigStoreFactory();
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

  @Test
  public void testLoadingFromAPropertyFileUsingRawData(TestContext context) {
    Async async = context.async();
    retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions()
      .addStore(new ConfigStoreOptions()
        .setFormat("properties")
        .setType("file")
        .setConfig(new JsonObject().put("path", "src/test/resources/file/raw.properties").put("raw-data", true))
      )
    );
    retriever.getConfig().onComplete(ar -> {
      assertThat(ar.succeeded()).isTrue();
      JsonObject json = ar.result();
      assertThat(json.getString("key")).isEqualTo("value");
      assertThat(json.getString("name")).isEqualTo("123456789012345678901234567890123456789012345678901234567890");
      assertThat(json.getString("true")).isEqualTo("true");
      assertThat(json.getString("false")).isEqualTo("false");
      assertThat(json.getString("missing")).isNull();
      assertThat(json.getString("int")).isEqualTo("5");
      assertThat(json.getString("float")).isEqualTo("25.3");
      async.complete();
    });
  }

}

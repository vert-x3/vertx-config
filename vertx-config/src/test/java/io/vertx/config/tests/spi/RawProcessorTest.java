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

import com.fasterxml.jackson.core.JsonParseException;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class RawProcessorTest {

  ConfigRetriever retriever;
  private Vertx vertx;

  @Before
  public void setUp(TestContext tc) {
    vertx = Vertx.vertx();
    vertx.exceptionHandler(tc.exceptionHandler());
  }


  @After
  public void tearDown() {
    retriever.close();
    vertx.close();
  }

  @Test
  public void testWithFiles(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions()
            .addStore(
                new ConfigStoreOptions()
                    .setType("file")
                    .setFormat("raw")
                    .setConfig(
                        new JsonObject()
                            .put("path", "src/test/resources/raw/username")
                            .put("raw.key", "username")))
            .addStore(
                new ConfigStoreOptions()
                    .setType("file")
                    .setFormat("raw")
                    .setConfig(
                        new JsonObject()
                            .put("path", "src/test/resources/raw/password")
                            .put("raw.key", "pwd"))
            )
    );

    retriever.getConfig().onComplete(ar -> {
      assertThat(ar.result()).isNotNull().isNotEmpty();
      assertThat(ar.result().getString("username")).isEqualTo("admin");
      assertThat(ar.result().getString("pwd")).isEqualTo("c2VjcmV0");
      async.complete();
    });
  }

  @Test
  public void testWithJson(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions()
            .addStore(
                new ConfigStoreOptions()
                    .setType("file")
                    .setFormat("raw")
                    .setConfig(
                        new JsonObject()
                            .put("path", "src/test/resources/raw/some-json.json")
                            .put("raw.type", "json-object")
                            .put("raw.key", "some-json")))
    );

    retriever.getConfig().onComplete(ar -> {
      assertThat(ar.result()).isNotNull().isNotEmpty();
      assertThat(ar.result().getJsonObject("some-json").encode()).contains("foo", "bar", "num", "1");
      async.complete();
    });
  }

  @Test
  public void testWithJsonArray(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions()
            .addStore(
                new ConfigStoreOptions()
                    .setType("file")
                    .setFormat("raw")
                    .setConfig(
                        new JsonObject()
                            .put("path", "src/test/resources/raw/some-json-array.json")
                            .put("raw.type", "json-array")
                            .put("raw.key", "some-json")))
    );

    retriever.getConfig().onComplete(ar -> {
      assertThat(ar.result()).isNotNull().isNotEmpty();
      assertThat(ar.result().getJsonArray("some-json").encode()).contains("1", "2", "3");
      async.complete();
    });
  }

  @Test
  public void testWithBinary(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions()
            .addStore(
                new ConfigStoreOptions()
                    .setType("file")
                    .setFormat("raw")
                    .setConfig(
                        new JsonObject()
                            .put("path", "src/test/resources/raw/logo-white-big.png")
                            .put("raw.type", "binary")
                            .put("raw.key", "logo")))
    );

    retriever.getConfig().onComplete(ar -> {
      assertThat(ar.result()).isNotNull().isNotEmpty();
      assertThat(ar.result().getBinary("logo")).isNotEmpty();
      async.complete();
    });
  }

  @Test
  public void testWithMissingKey(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions()
            .addStore(
                new ConfigStoreOptions()
                    .setType("file")
                    .setFormat("raw")
                    .setConfig(
                        new JsonObject()
                            .put("path", "src/test/resources/raw/logo-white-big.png")
                            .put("raw.type", "binary")))
    );

    retriever.getConfig().onComplete(ar -> {
      assertThat(ar.failed());
      assertThat(ar.cause().getMessage()).contains("raw.key");
      async.complete();
    });
  }

  @Test
  public void testWithUnrecognizedType(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions()
            .addStore(
                new ConfigStoreOptions()
                    .setType("file")
                    .setFormat("raw")
                    .setConfig(
                        new JsonObject()
                            .put("path", "src/test/resources/raw/logo-white-big.png")
                            .put("raw.key", "any")
                            .put("raw.type", "not a valid type")))
    );

    retriever.getConfig().onComplete(ar -> {
      assertThat(ar.failed());
      assertThat(ar.cause().getMessage()).contains("raw.type");
      async.complete();
    });
  }

  @Test
  public void testWithBrokenJson(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions()
            .addStore(
                new ConfigStoreOptions()
                    .setType("file")
                    .setFormat("raw")
                    .setConfig(
                        new JsonObject()
                            .put("path", "src/test/resources/raw/username")
                            .put("raw.type", "json-object")
                            .put("raw.key", "some-json")))
    );

    retriever.getConfig().onComplete(ar -> {
      assertThat(ar.failed());
      assertThat(ar.cause())
        .isInstanceOf(DecodeException.class)
        .hasRootCauseInstanceOf(JsonParseException.class);
      async.complete();
    });
  }



}

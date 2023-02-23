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

package io.vertx.config.hocon;

import com.typesafe.config.ConfigException;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystemException;
import io.vertx.core.json.JsonObject;
import io.vertx.config.ConfigStoreOptions;
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
public class HoconProcessorTest {

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
  public void testEmptyHocon(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions().addStore(
            new ConfigStoreOptions()
                .setType("file")
                .setFormat("hocon")
                .setConfig(new JsonObject().put("path", "src/test/resources/empty.conf"))));

    retriever.getConfig(ar -> {
      assertThat(ar.result()).isNotNull();
      assertThat(ar.result()).isEmpty();
      async.complete();
    });
  }

  @Test
  public void testWithTextFile(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions().addStore(
            new ConfigStoreOptions()
                .setType("file")
                .setFormat("hocon")
                .setConfig(new JsonObject().put("path", "src/test/resources/some-text.txt"))));

    retriever.getConfig(ar -> {
      assertThat(ar.failed()).isTrue();
      assertThat(ar.cause()).isNotNull().isInstanceOf(ConfigException.class);
      async.complete();
    });
  }

  @Test
  public void testWithMissingFile(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions().addStore(
            new ConfigStoreOptions()
                .setType("file")
                .setFormat("hocon")
                .setConfig(new JsonObject().put("path", "src/test/resources/some-missing-file.conf"))));

    retriever.getConfig(ar -> {
      assertThat(ar.failed()).isTrue();
      assertThat(ar.cause()).isNotNull().isInstanceOf(FileSystemException.class);
      async.complete();
    });
  }

  @Test
  public void testSimpleHoconConfiguration(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions().addStore(
            new ConfigStoreOptions()
                .setType("file")
                .setFormat("hocon")
                .setConfig(new JsonObject().put("path", "src/test/resources/simple.conf"))));

    retriever.getConfig(ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
      }
      assertThat(ar.succeeded()).isTrue();
      JsonObject json = ar.result();
      assertThat(json.getString("key")).isEqualTo("value");

      assertThat(json.getBoolean("true")).isTrue();
      assertThat(json.getBoolean("false")).isFalse();

      assertThat(json.getString("missing")).isNull();

      assertThat(json.getInteger("int")).isEqualTo(5);
      assertThat(json.getDouble("float")).isEqualTo(25.3);

      assertThat(json.getJsonArray("array").size()).isEqualTo(3);
      assertThat(json.getJsonArray("array").contains(1)).isTrue();
      assertThat(json.getJsonArray("array").contains(2)).isTrue();
      assertThat(json.getJsonArray("array").contains(3)).isTrue();

      assertThat(json.getJsonObject("sub").getString("foo")).isEqualTo("bar");

      assertThat(json.getString("some-macro")).isEqualToIgnoringCase("hello clement");

      async.complete();
    });
  }

  @Test
  public void testSimpleJsonConfiguration(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions().addStore(
            new ConfigStoreOptions()
                .setType("file")
                .setFormat("hocon")
                .setConfig(new JsonObject().put("path", "src/test/resources/regular.json"))));

    retriever.getConfig(ar -> {
      assertThat(ar.succeeded()).isTrue();
      JsonObject json = ar.result();
      assertThat(json.getString("key")).isEqualTo("value");

      assertThat(json.getBoolean("true")).isTrue();
      assertThat(json.getBoolean("false")).isFalse();

      assertThat(json.getString("missing")).isNull();

      assertThat(json.getInteger("int")).isEqualTo(5);
      assertThat(json.getDouble("float")).isEqualTo(25.3);

      assertThat(json.getJsonArray("array").size()).isEqualTo(3);
      assertThat(json.getJsonArray("array").contains(1)).isTrue();
      assertThat(json.getJsonArray("array").contains(2)).isTrue();
      assertThat(json.getJsonArray("array").contains(3)).isTrue();

      assertThat(json.getJsonObject("sub").getString("foo")).isEqualTo("bar");

      async.complete();
    });
  }

  @Test
  public void testSimplePropertiesConfiguration(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions().addStore(
            new ConfigStoreOptions()
                .setType("file")
                .setFormat("hocon")
                .setConfig(new JsonObject().put("path", "src/test/resources/regular.properties"))));

    retriever.getConfig(ar -> {
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
  public void testComplex(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions().addStore(
            new ConfigStoreOptions()
                .setType("file")
                .setFormat("hocon")
                .setConfig(new JsonObject().put("path", "src/test/resources/complex.conf"))));

    retriever.getConfig(ar -> {
      assertThat(ar.succeeded()).isTrue();
      JsonObject json = ar.result();
      JsonObject complex = json.getJsonObject("complex");
      assertThat(complex).isNotNull();
      JsonObject bar = complex.getJsonObject("bar");
      assertThat(bar).isNotNull();
      assertThat(bar.getString("timeout")).isEqualTo("10ms");
      JsonObject foo = complex.getJsonObject("foo");
      assertThat(foo.getInteger("bar")).isEqualTo(10);
      assertThat(foo.getInteger("baz")).isEqualTo(12);
      assertThat(foo.getInteger("bob")).isEqualTo(13);
      assertThat(foo.getString("timeout")).isEqualTo("10ms");

      assertThat(complex.getJsonObject("inherited").getInteger("a")).isEqualTo(42);
      assertThat(complex.getJsonObject("inherited").getInteger("b")).isEqualTo(43);
      assertThat(complex.getJsonObject("inherited").getInteger("c")).isEqualTo(6);

      assertThat(complex.getString("something")).contains("from my configuration");

      JsonObject nested = json.getJsonObject("nested");
      assertThat(nested).isNotNull();
      assertThat(nested.getString("from")).isEqualToIgnoringCase("json");
      assertThat(nested.getString("message")).isEqualToIgnoringCase("hello");
      assertThat(nested.getJsonObject("some").getJsonObject("nested")
          .getJsonObject("conf").getString("a")).isEqualToIgnoringCase("a");

      async.complete();
    });

  }

  @Test
  public void testSimpleHoconConfigurationEnvOverride(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigRetriever.create(vertx,
      new ConfigRetrieverOptions().addStore(
        new ConfigStoreOptions()
          .setType("file")
          .setFormat("hocon")
          .setConfig(new JsonObject().put("path", "src/test/resources/simple.conf")
            .put("hocon.env.override", true))));

    retriever.getConfig(ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
      }
      assertThat(ar.succeeded()).isTrue();
      JsonObject json = ar.result();
      assertThat(json.getString("key")).isEqualTo("value");
      assertThat(json.getBoolean("true")).isTrue();
      assertThat(json.getBoolean("false")).isFalse();
      assertThat(json.getString("missing")).isNull();
      assertThat(json.getDouble("float")).isEqualTo(25.3);
      assertThat(json.getJsonArray("array").size()).isEqualTo(3);
      assertThat(json.getJsonArray("array").contains(1)).isTrue();
      assertThat(json.getJsonArray("array").contains(2)).isTrue();
      assertThat(json.getJsonArray("array").contains(3)).isTrue();

      assertThat(json.getInteger("int")).isEqualTo(10);
      assertThat(json.getJsonObject("sub").getString("foo")).isEqualTo("bar2");
      assertThat(json.getString("some-macro")).isEqualToIgnoringCase("hocon");

      async.complete();
    });
  }

}

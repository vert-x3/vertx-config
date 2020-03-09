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

package io.vertx.config;

import io.vertx.config.impl.spi.ConfigChecker;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class ConfigurationRetrieverTest {

  private Vertx vertx;
  private ConfigRetriever retriever;

  @Before
  public void setUp(TestContext tc) {
    vertx = Vertx.vertx();
    vertx.exceptionHandler(tc.exceptionHandler());

    System.setProperty("foo", "bar");
  }

  @After
  public void tearDown() {
    retriever.close();
    vertx.close();
    System.clearProperty("key");
    System.clearProperty("foo");
    System.clearProperty("vertx-config-path");
  }

  private static ConfigRetrieverOptions addStores(ConfigRetrieverOptions options) {
    return options
      .addStore(
        new ConfigStoreOptions()
          .setType("file")
          .setConfig(new JsonObject().put("path", "src/test/resources/file/regular.json")))
      .addStore(
        new ConfigStoreOptions()
          .setType("sys")
          .setConfig(new JsonObject().put("cache", false)));
  }

  private static ConfigRetrieverOptions addReversedStores(ConfigRetrieverOptions options) {
    return options
      .addStore(
        new ConfigStoreOptions()
          .setType("sys")
          .setConfig(new JsonObject().put("cache", false)))
      .addStore(
        new ConfigStoreOptions()
          .setType("file")
          .setConfig(new JsonObject().put("path", "src/test/resources/file/regular.json")));
  }

  @Test
  public void testLoading(TestContext tc) {
    retriever = ConfigRetriever.create(vertx,
      addStores(new ConfigRetrieverOptions()));
    Async async = tc.async();

    retriever.getConfig(ar -> {
      ConfigChecker.check(ar);
      assertThat(ar.result().getString("foo")).isEqualToIgnoringCase("bar");
      ConfigChecker.check(retriever.getCachedConfig());
      async.complete();
    });
  }

  @Test
  public void testLoadingWithProcessor(TestContext tc) {
    retriever = ConfigRetriever.create(vertx,
      addStores(new ConfigRetrieverOptions()))
    .setConfigurationProcessor(json -> {
      if (json.containsKey("foo")) {
        json.put("foo", json.getString("foo").toUpperCase());
      }
      return json;
    });
    Async async = tc.async();

    retriever.getConfig(ar -> {
      ConfigChecker.check(ar);
      assertThat(ar.result().getString("foo")).isEqualToIgnoringCase("BAR");
      ConfigChecker.check(retriever.getCachedConfig());
      async.complete();
    });
  }

  @Test
  public void testLoadingWithProcessorFailure(TestContext tc) {
    retriever = ConfigRetriever.create(vertx,
      addStores(new ConfigRetrieverOptions()))
      .setConfigurationProcessor(json -> {
        throw new RuntimeException("failed");
      });
    Async async = tc.async();

    retriever.getConfig(ar -> {
      tc.assertTrue(ar.failed());
      async.complete();
    });
  }

  @Test
  public void testLoadingWithFuturePolyglotVersion(TestContext tc) {
    retriever = ConfigRetriever.create(vertx,
      addStores(new ConfigRetrieverOptions()));
    Async async = tc.async();

    retriever.getConfig().onComplete(ar -> {
      ConfigChecker.check(ar);
      assertThat(ar.result().getString("foo")).isEqualToIgnoringCase("bar");
      ConfigChecker.check(retriever.getCachedConfig());
      async.complete();
    });
  }

  @Test
  public void testLoadingWithFutureJAvaVersion(TestContext tc) {
    retriever = ConfigRetriever.create(vertx,
      addStores(new ConfigRetrieverOptions()));
    Async async = tc.async();

    retriever.getConfig().onComplete(ar -> {
      ConfigChecker.check(ar);
      assertThat(ar.result().getString("foo")).isEqualToIgnoringCase("bar");
      ConfigChecker.check(retriever.getCachedConfig());
      async.complete();
    });
  }

  @Test
  public void testDefaultLoading(TestContext tc) {
    Async async = tc.async();
    vertx.runOnContext(v -> {
      vertx.getOrCreateContext().config().put("hello", "hello");
      System.setProperty("foo", "bar");
      retriever = ConfigRetriever.create(vertx);

      retriever.getConfig(ar -> {
        assertThat(ar.result().getString("foo")).isEqualToIgnoringCase("bar");
        assertThat(ar.result().getString("hello")).isEqualToIgnoringCase("hello");
        assertThat(ar.result().getString("PATH")).isNotNull();
        async.complete();
      });
    });
  }

  @Test
  public void testDefaultStoreWithVertxConfigPath(TestContext tc) {
    Async async = tc.async();
    System.setProperty("vertx-config-path", "src/test/resources/file/regular.json");
    retriever = ConfigRetriever.create(vertx);
    retriever.getConfig(ar -> {
      assertThat(ar.result().getString("key")).isEqualToIgnoringCase("value");
      assertThat(ar.result().getString("PATH")).isNotNull();
      async.complete();
    });
  }

  @Test
  public void testDefaultStoreWithDefaultConfigFromFileSystem(TestContext tc) {
    Async async = tc.async();
    File conf = new File("conf");
    conf.mkdirs();

    try {
      vertx.fileSystem()
        .writeFileBlocking("conf" + File.separator + "config.json",
          new JsonObject()
            .put("name", "config.json")
            .put("some-key", "some-message")
            .put("from", "file-system").toBuffer());

      retriever = ConfigRetriever.create(vertx);
      retriever.getConfig(ar -> {
        assertThat(ar.result().getString("name")).isEqualToIgnoringCase("config.json");
        assertThat(ar.result().getString("some-key")).isEqualToIgnoringCase("some-message");
        assertThat(ar.result().getString("from")).isEqualToIgnoringCase("file-system");
        assertThat(ar.result().getString("foo")).isEqualToIgnoringCase("bar");
        assertThat(ar.result().getString("PATH")).isNotNull();
        async.complete();
      });

      async.await(10_000);
    } finally {
      cleanupConf();
    }
  }

  private void cleanupConf() {
    vertx.fileSystem().deleteRecursiveBlocking("conf", true);
  }

  @Test
  public void testDefaultStoreWithDefaultConfigFromClassPath(TestContext tc) {
    cleanupConf();

    Async async = tc.async();

    retriever = ConfigRetriever.create(vertx);
    retriever.getConfig(ar -> {
      assertThat(ar.result().getString("name")).isEqualToIgnoringCase("config.json");
      assertThat(ar.result().getString("some-key")).isEqualToIgnoringCase("some-message");
      assertThat(ar.result().getString("from")).isEqualToIgnoringCase("class-path");
      assertThat(ar.result().getString("foo")).isEqualToIgnoringCase("bar");
      assertThat(ar.result().getString("PATH")).isNotNull();
      async.complete();
    });
  }

  @Test
  public void testDefaultStoreWithSystemPropertyConfigFromClassPath(TestContext tc) {
    cleanupConf();

    Async async = tc.async();

    System.setProperty("vertx-config-path", "conf/config2.json");

    try {
      retriever = ConfigRetriever.create(vertx);
      retriever.getConfig(ar -> {
        assertThat(ar.result().getString("name")).isEqualToIgnoringCase("config2.json");
        assertThat(ar.result().getString("some-key")).isEqualToIgnoringCase("some-message");
        assertThat(ar.result().getString("from")).isEqualToIgnoringCase("class-path");
        assertThat(ar.result().getString("foo")).isEqualToIgnoringCase("bar");
        assertThat(ar.result().getString("PATH")).isNotNull();
        async.complete();
      });

      async.await(10_000);
    } finally {
      System.clearProperty("vertx-config-path");
    }
  }

  @Test
  public void testDefaultLoadingWithOverloading(TestContext tc) {
    Async async = tc.async();
    vertx.runOnContext(v -> {
      vertx.getOrCreateContext().config()
        .put("hello", "hello")
        .put("foo", "bar");
      System.setProperty("foo", "baz");
      retriever = ConfigRetriever.create(vertx);

      retriever.getConfig(ar -> {
        assertThat(ar.result().getString("foo")).isEqualToIgnoringCase("baz");
        assertThat(ar.result().getString("hello")).isEqualToIgnoringCase("hello");
        assertThat(ar.result().getString("PATH")).isNotNull();
        async.complete();
      });
    });
  }

  @Test
  public void testOverloading(TestContext tc) {
    System.setProperty("key", "new-value");
    retriever = ConfigRetriever.create(vertx,
      addStores(new ConfigRetrieverOptions()));
    Async async = tc.async();

    retriever.getConfig(ar -> {
      assertThat(ar.result().getString("key")).isEqualToIgnoringCase("new-value");
      async.complete();
    });
  }

  @Test
  public void testReversedOverloading(TestContext tc) {
    System.setProperty("key", "new-value");
    retriever = ConfigRetriever.create(vertx, addReversedStores(new ConfigRetrieverOptions()));
    Async async = tc.async();
    retriever.getConfig(ar -> {
      assertThat(ar.result().getString("key")).isEqualToIgnoringCase("value");
      async.complete();
    });
  }

  @Test
  public void testExceptionWhenCallbackFailed(TestContext tc) {
    List<ConfigStoreOptions> options = new ArrayList<>();
    options.add(new ConfigStoreOptions().setType("file")
      .setConfig(new JsonObject().put("path", "src/test/resources/file/regular.json")));
    retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions().setStores(options));

    AtomicReference<Throwable> reference = new AtomicReference<>();
    vertx.exceptionHandler(reference::set);

    retriever.getConfig(ar -> {
      tc.assertTrue(ar.succeeded());
      tc.assertNotNull(ar.result());

      // Class cast exception here - on purpose
      ar.result().getBoolean("int");
    });

    await().untilAtomic(reference, is(notNullValue()));
    assertThat(reference.get()).isInstanceOf(ClassCastException.class).hasMessageContaining("java.lang.Integer");
  }

  @Test
  public void testWithOptionalAndSuccess(TestContext tc) {
    List<ConfigStoreOptions> options = new ArrayList<>();
    options.add(new ConfigStoreOptions().setType("file")
      .setConfig(new JsonObject().put("path", "src/test/resources/file/regular.json")).setOptional(true));
    retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions().setStores(options));

    AtomicReference<Throwable> reference = new AtomicReference<>();
    vertx.exceptionHandler(reference::set);

    retriever.getConfig(ar -> {
      tc.assertTrue(ar.succeeded());
      tc.assertNotNull(ar.result());
      assertThat(ar.result().getString("key")).isEqualTo("value");
    });
  }

  @Test
  public void testWithOptionalAndRetrieveFailure(TestContext tc) {
    List<ConfigStoreOptions> options = new ArrayList<>();
    options.add(new ConfigStoreOptions().setType("file")
      .setConfig(new JsonObject().put("path", "src/test/resources/file/missing.json")).setOptional(true));
    retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions().setStores(options));

    AtomicReference<Throwable> reference = new AtomicReference<>();
    vertx.exceptionHandler(reference::set);

    retriever.getConfig(ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
      }
      tc.assertTrue(ar.succeeded());
      tc.assertNotNull(ar.result());
      assertThat(ar.result()).isEmpty();
    });
  }

  @Test
  public void testWithOptionalAndProcessingFailure(TestContext tc) {
    List<ConfigStoreOptions> options = new ArrayList<>();
    options.add(new ConfigStoreOptions().setType("file")
      .setConfig(new JsonObject().put("path", "src/test/resources/file/regular.json"))
      .setOptional(true)
      .setFormat("properties"));
    retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions().setStores(options));

    AtomicReference<Throwable> reference = new AtomicReference<>();
    vertx.exceptionHandler(reference::set);

    retriever.getConfig(ar -> {
      tc.assertTrue(ar.succeeded());
      tc.assertNotNull(ar.result());
      assertThat(ar.result()).isEmpty();
    });
  }

}

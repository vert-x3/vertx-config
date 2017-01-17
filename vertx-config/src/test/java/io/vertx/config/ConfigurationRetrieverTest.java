package io.vertx.config;

import io.vertx.config.impl.spi.ConfigurationChecker;
import io.vertx.core.Vertx;
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
public class ConfigurationRetrieverTest {

  private Vertx vertx;
  private ConfigurationRetriever retriever;

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
  }

  private static ConfigurationRetrieverOptions addStores(ConfigurationRetrieverOptions options) {
    return options
        .addStore(
            new ConfigurationStoreOptions()
                .setType("file")
                .setConfig(new JsonObject().put("path", "src/test/resources/file/regular.json")))
        .addStore(
            new ConfigurationStoreOptions()
                .setType("sys")
                .setConfig(new JsonObject().put("cache", false)));
  }

  private static ConfigurationRetrieverOptions addReversedStores(ConfigurationRetrieverOptions options) {
    return options
        .addStore(
            new ConfigurationStoreOptions()
                .setType("sys")
                .setConfig(new JsonObject().put("cache", false)))
        .addStore(
            new ConfigurationStoreOptions()
                .setType("file")
                .setConfig(new JsonObject().put("path", "src/test/resources/file/regular.json")));
  }

  @Test
  public void testLoading(TestContext tc) {
    retriever = ConfigurationRetriever.create(vertx,
        addStores(new ConfigurationRetrieverOptions()));
    Async async = tc.async();

    retriever.getConfiguration(ar -> {
      ConfigurationChecker.check(ar);
      assertThat(ar.result().getString("foo")).isEqualToIgnoringCase("bar");
      ConfigurationChecker.check(retriever.getCachedConfiguration());
      async.complete();
    });
  }

  @Test
  public void testLoadingWithFuturePolyglotVersion(TestContext tc) {
    retriever = ConfigurationRetriever.create(vertx,
        addStores(new ConfigurationRetrieverOptions()));
    Async async = tc.async();

    retriever.<JsonObject>getConfigurationFuture().setHandler(ar -> {
      ConfigurationChecker.check(ar);
      assertThat(ar.result().getString("foo")).isEqualToIgnoringCase("bar");
      ConfigurationChecker.check(retriever.getCachedConfiguration());
      async.complete();
    });
  }

  @Test
  public void testLoadingWithFutureJAvaVersion(TestContext tc) {
    retriever = ConfigurationRetriever.create(vertx,
        addStores(new ConfigurationRetrieverOptions()));
    Async async = tc.async();

    retriever.getConfigurationFuture().setHandler(ar -> {
      ConfigurationChecker.check(ar);
      assertThat(ar.result().getString("foo")).isEqualToIgnoringCase("bar");
      ConfigurationChecker.check(retriever.getCachedConfiguration());
      async.complete();
    });
  }

  @Test
  public void testDefaultLoading(TestContext tc) {
    Async async = tc.async();
    vertx.runOnContext(v -> {
      vertx.getOrCreateContext().config().put("hello", "hello");
      System.setProperty("foo", "bar");
      retriever = ConfigurationRetriever.create(vertx);

      retriever.getConfiguration(ar -> {
        assertThat(ar.result().getString("foo")).isEqualToIgnoringCase("bar");
        assertThat(ar.result().getString("hello")).isEqualToIgnoringCase("hello");
        assertThat(ar.result().getString("PATH")).isNotNull();
        async.complete();
      });
    });
  }

  @Test
  public void testDefaultLoadingWithOverloading(TestContext tc) {
    Async async = tc.async();
    vertx.runOnContext(v -> {
      vertx.getOrCreateContext().config()
          .put("hello", "hello")
          .put("foo", "bar");
      System.setProperty("foo", "baz");
      retriever = ConfigurationRetriever.create(vertx);

      retriever.getConfiguration(ar -> {
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
    retriever = ConfigurationRetriever.create(vertx,
        addStores(new ConfigurationRetrieverOptions()));
    Async async = tc.async();

    retriever.getConfiguration(ar -> {
      assertThat(ar.result().getString("key")).isEqualToIgnoringCase("new-value");
      async.complete();
    });
  }

  @Test
  public void testReversedOverloading(TestContext tc) {
    System.setProperty("key", "new-value");
    retriever = ConfigurationRetriever.create(vertx, addReversedStores(new ConfigurationRetrieverOptions()));
    Async async = tc.async();
    retriever.getConfiguration(ar -> {
      assertThat(ar.result().getString("key")).isEqualToIgnoringCase("value");
      async.complete();
    });
  }

}
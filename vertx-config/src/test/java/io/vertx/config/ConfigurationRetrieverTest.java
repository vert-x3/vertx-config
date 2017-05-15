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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.jayway.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;
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
  public void testLoadingWithFuturePolyglotVersion(TestContext tc) {
    retriever = ConfigRetriever.create(vertx,
        addStores(new ConfigRetrieverOptions()));
    Async async = tc.async();

    ConfigRetriever.getConfigAsFuture(retriever).setHandler(ar -> {
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

    ConfigRetriever.getConfigAsFuture(retriever).setHandler(ar -> {
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
      ar.result().getString("int");
    });

    await().untilAtomic(reference, is(notNullValue()));
    assertThat(reference.get()).isInstanceOf(ClassCastException.class).hasMessageContaining("java.lang.Integer");
  }

}

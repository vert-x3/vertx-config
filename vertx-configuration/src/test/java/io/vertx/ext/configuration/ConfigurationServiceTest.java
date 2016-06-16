package io.vertx.ext.configuration;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.configuration.impl.spi.ConfigurationChecker;
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
public class ConfigurationServiceTest {

  private Vertx vertx;
  private ConfigurationService service;

  @Before
  public void setUp(TestContext tc) {
    vertx = Vertx.vertx();
    vertx.exceptionHandler(tc.exceptionHandler());

    System.setProperty("foo", "bar");
  }

  @After
  public void tearDown() {
    service.close();
    vertx.close();
    System.clearProperty("key");
    System.clearProperty("foo");
  }

  private static ConfigurationServiceOptions addStores(ConfigurationServiceOptions options) {
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

  private static ConfigurationServiceOptions addReversedStores(ConfigurationServiceOptions options) {
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
    service = ConfigurationService.create(vertx, addStores(new ConfigurationServiceOptions()));
    Async async = tc.async();

    service.getConfiguration(ar -> {
      ConfigurationChecker.check(ar);
      assertThat(ar.result().getString("foo")).isEqualToIgnoringCase("bar");
      ConfigurationChecker.check(service.getCachedConfiguration());
      async.complete();
    });

  }

  @Test
  public void testDefaultLoading(TestContext tc) {
    Async async = tc.async();
    vertx.runOnContext(v -> {
      vertx.getOrCreateContext().config().put("hello", "hello");
      System.setProperty("foo", "bar");
      service = ConfigurationService.create(vertx);

      service.getConfiguration(ar -> {
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
      service = ConfigurationService.create(vertx);

      service.getConfiguration(ar -> {
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
    service = ConfigurationService.create(vertx, addStores(new ConfigurationServiceOptions()));
    Async async = tc.async();

    service.getConfiguration(ar -> {
      assertThat(ar.result().getString("key")).isEqualToIgnoringCase("new-value");
      async.complete();
    });
  }

  @Test
  public void testReversedOverloading(TestContext tc) {
    System.setProperty("key", "new-value");
    service = ConfigurationService.create(vertx, addReversedStores(new ConfigurationServiceOptions()));
    Async async = tc.async();
    service.getConfiguration(ar -> {
      assertThat(ar.result().getString("key")).isEqualToIgnoringCase("value");
      async.complete();
    });
  }

}
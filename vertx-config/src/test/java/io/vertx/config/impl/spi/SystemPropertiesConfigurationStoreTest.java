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
public class SystemPropertiesConfigurationStoreTest extends ConfigurationStoreTestBase {

  @Before
  public void init() {
    factory = new SystemPropertiesConfigurationStore();
    System.setProperty("key", "value");
    System.setProperty("sub", "{\"foo\":\"bar\"}");
    System.setProperty("array", "[1, 2, 3]");
    System.setProperty("int", "5");
    System.setProperty("float", "25.3");
    System.setProperty("true", "true");
    System.setProperty("false", "false");
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
      ConfigurationChecker.check(ar);

      // By default, the configuration is cached, try adding some entries
      System.setProperty("new", "some new value");
      getJsonConfiguration(vertx, store, ar2 -> {
        ConfigurationChecker.check(ar2);
        assertThat(ar2.result().getString("new")).isNull();
        async.complete();
      });
    });
  }

  @Test
  public void testLoadingFromSystemPropertiesWithoutCache(TestContext context) {
    Async async = context.async();
    store = factory.create(vertx, new JsonObject().put("cache", false));
    getJsonConfiguration(vertx, store, ar -> {
      ConfigurationChecker.check(ar);
      System.setProperty("new", "some new value");
      getJsonConfiguration(vertx, store, ar2 -> {
        ConfigurationChecker.check(ar2);
        assertThat(ar2.result().getString("new")).isEqualToIgnoringCase("some new value");
        async.complete();
      });
    });
  }

}
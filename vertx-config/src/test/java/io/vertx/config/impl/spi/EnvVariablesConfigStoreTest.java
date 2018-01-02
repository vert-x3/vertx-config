package io.vertx.config.impl.spi;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class EnvVariablesConfigStoreTest extends ConfigStoreTestBase {

  @Before
  public void init() {
    factory = new EnvVariablesConfigStore();
    store = factory.create(vertx, new JsonObject());
  }

  @Test
  public void testName() {
    assertThat(factory.name()).isNotNull().isEqualTo("env");
  }

  @Test
  public void testLoadingFromEnv(TestContext context) {
    Async async = context.async();
    getJsonConfiguration(vertx, store, ar -> {
      assertThat(ar.succeeded()).isTrue();
      assertThat(ar.result().getString("PATH")).isNotNull();
      async.complete();
    });
  }

  @Test
  public void testLoadingFromEnvWithNullKeySet(TestContext context) {
    Async async = context.async();
    store = factory.create(vertx, new JsonObject().put("keys", (JsonArray) null));
    getJsonConfiguration(vertx, store, ar -> {
      assertThat(ar.succeeded()).isTrue();
      assertThat(ar.result().getString("PATH")).isNotNull();
      async.complete();
    });
  }

  @Test(expected = ClassCastException.class)
  public void testLoadingFromEnvWithInvalidKeySet() {
    store = factory.create(vertx, new JsonObject().put("keys", "invalid"));
  }

  /**
   * Reproducer for https://github.com/vert-x3/vertx-config/issues/31.
   */
  @Test
  public void testLoadingFromEnvWithKeySet(TestContext context) {
    Async async = context.async();
    store = factory.create(vertx, new JsonObject().put("keys", new JsonArray().add("USER").add("HOME")));
    getJsonConfiguration(vertx, store, ar -> {
      assertThat(ar.succeeded()).isTrue();
      assertThat(ar.result().getString("PATH")).isNull();
      assertThat(ar.result().getString("USER")).isNotNull();
      assertThat(ar.result().getString("HOME")).isNotNull();
      async.complete();
    });
  }
}

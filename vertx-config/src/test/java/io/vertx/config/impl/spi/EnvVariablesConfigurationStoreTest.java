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
public class EnvVariablesConfigurationStoreTest extends ConfigurationStoreTestBase {

  @Before
  public void init() {
    factory = new EnvVariablesConfigurationStore();
    store = factory.create(vertx, new JsonObject());
  }

  @Test
  public void testName() {
    assertThat(factory.name()).isNotNull().isEqualTo("env");
  }

  @Test
  public void testLoadingFromSystemProperties(TestContext context) {
    Async async = context.async();
    getJsonConfiguration(vertx, store, ar -> {
      assertThat(ar.succeeded()).isTrue();
      assertThat(ar.result().getString("PATH")).isNotNull();
      async.complete();
    });
  }
}
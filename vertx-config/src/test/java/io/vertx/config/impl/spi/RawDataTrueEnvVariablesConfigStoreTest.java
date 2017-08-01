package io.vertx.config.impl.spi;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(VertxUnitRunner.class)
@PrepareForTest(EnvVariablesConfigStore.class)
public class RawDataTrueEnvVariablesConfigStoreTest extends ConfigStoreTestBase {
  private static final Map<String, String> ENV = new HashMap<>();
  private static final String KEY_1 = "SOME_NUMBER";
  private static final String VAL_1 = "1234567890";
  private static final String KEY_2 = "SOME_BOOLEAN";
  private static final String VAL_2 = "true";

  static {
    ENV.put(KEY_1, VAL_1);
    ENV.put(KEY_2, VAL_2);
  }

  @Before
  public void init() {
    PowerMockito.mockStatic(System.class);
    PowerMockito.when(System.getenv()).thenReturn(ENV);
    factory = new EnvVariablesConfigStore();
    store = factory.create(vertx, new JsonObject().put("raw-data", true));
  }

  @Test
  public void testName() {
    assertThat(factory.name()).isNotNull().isEqualTo("env");
  }

  @Test
  public void testLoadingFromEnvironmentVariables(TestContext context) {
    Async async = context.async();
    getJsonConfiguration(vertx, store, ar -> {
      assertThat(ar.succeeded()).isTrue();
      assertThat(ar.result().getString(KEY_1)).isEqualTo(VAL_1);
      assertThat(ar.result().getString(KEY_2)).isEqualTo(VAL_2);
      async.complete();
    });
  }
}

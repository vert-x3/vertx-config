package io.vertx.config.impl.spi;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Before;
import org.junit.Test;

import static com.jayway.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class JsonConfigStoreTest extends ConfigStoreTestBase {

  private static final String JSON = "{\n" +
      "  \"key\": \"value\",\n" +
      "  \"sub\": {\n" +
      "    \"foo\": \"bar\"\n" +
      "  },\n" +
      "  \"array\": [\n" +
      "    1,\n" +
      "    2,\n" +
      "    3\n" +
      "  ],\n" +
      "  \"int\": 5,\n" +
      "  \"float\": 25.3,\n" +
      "  \"true\": true,\n" +
      "  \"false\": false\n" +
      "}";

  @Before
  public void init() {
    factory = new JsonConfigStoreFactory();
  }


  @Test
  public void testWithConfiguration(TestContext tc) {
    Async async = tc.async();
    store = factory.create(vertx, new JsonObject(JSON));

    getJsonConfiguration(vertx, store, ar -> {
      ConfigChecker.check(ar);
      async.complete();
    });
  }

  @Test
  public void testName() {
    assertThat(factory.name()).isNotNull().isEqualTo("json");
  }

}
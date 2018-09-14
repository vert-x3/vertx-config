package io.vertx.config.impl.spi;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
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
 * @author Евгений Уткин (evgeny.utkin@mediascope.net)
 */
@RunWith(VertxUnitRunner.class)
public class PropertiesAsJsonStoreProcessorTest {

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
  public void testWithFiles(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigRetriever.create(vertx,
      new ConfigRetrieverOptions()
        .addStore(
          new ConfigStoreOptions()
            .setType("file")
            .setFormat("properties-as-json")
            .setConfig(
              new JsonObject()
                .put("path", "src/test/resources/file/jsonable.properties")))
    );

    JsonObject expected = new JsonObject()
      .put("server", new JsonObject()
        .put("port", 8080)
        .put("host", "http://localhost")
      )
      .put("some", new JsonObject()
        .put("double", new JsonObject().put("value", 1.0))
        .put("integer", new JsonObject().put("values", new JsonArray().add(1).add(2).add(3)))
      )
      .put("single", 0);

    System.out.println(expected.encodePrettily());


    retriever.getConfig(ar -> {
      JsonObject config = ar.result();
      System.out.println(config.encodePrettily());
      assertThat(config).isEqualTo(expected);
      async.complete();
    });
  }

}

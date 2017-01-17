package io.vertx.config.impl.spi;

import io.vertx.config.ConfigurationRetriever;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.config.ConfigurationRetrieverOptions;
import io.vertx.config.ConfigurationStoreOptions;
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
public class RawProcessorTest {

  ConfigurationRetriever retriever;
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
    retriever = ConfigurationRetriever.create(vertx,
        new ConfigurationRetrieverOptions()
            .addStore(
                new ConfigurationStoreOptions()
                    .setType("file")
                    .setFormat("raw")
                    .setConfig(
                        new JsonObject()
                            .put("path", "src/test/resources/raw/username")
                            .put("raw.key", "username")))
            .addStore(
                new ConfigurationStoreOptions()
                    .setType("file")
                    .setFormat("raw")
                    .setConfig(
                        new JsonObject()
                            .put("path", "src/test/resources/raw/password")
                            .put("raw.key", "pwd"))
            )
    );

    retriever.getConfiguration(ar -> {
      assertThat(ar.result()).isNotNull().isNotEmpty();
      assertThat(ar.result().getString("username")).isEqualTo("admin");
      assertThat(ar.result().getString("pwd")).isEqualTo("c2VjcmV0");
      async.complete();
    });
  }

  @Test
  public void testWithJson(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigurationRetriever.create(vertx,
        new ConfigurationRetrieverOptions()
            .addStore(
                new ConfigurationStoreOptions()
                    .setType("file")
                    .setFormat("raw")
                    .setConfig(
                        new JsonObject()
                            .put("path", "src/test/resources/raw/some-json.json")
                            .put("raw.type", "json-object")
                            .put("raw.key", "some-json")))
    );

    retriever.getConfiguration(ar -> {
      assertThat(ar.result()).isNotNull().isNotEmpty();
      assertThat(ar.result().getJsonObject("some-json").encode()).contains("foo", "bar", "num", "1");
      async.complete();
    });
  }

  @Test
  public void testWithJsonArray(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigurationRetriever.create(vertx,
        new ConfigurationRetrieverOptions()
            .addStore(
                new ConfigurationStoreOptions()
                    .setType("file")
                    .setFormat("raw")
                    .setConfig(
                        new JsonObject()
                            .put("path", "src/test/resources/raw/some-json-array.json")
                            .put("raw.type", "json-array")
                            .put("raw.key", "some-json")))
    );

    retriever.getConfiguration(ar -> {
      assertThat(ar.result()).isNotNull().isNotEmpty();
      assertThat(ar.result().getJsonArray("some-json").encode()).contains("1", "2", "3");
      async.complete();
    });
  }

  @Test
  public void testWithBinary(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigurationRetriever.create(vertx,
        new ConfigurationRetrieverOptions()
            .addStore(
                new ConfigurationStoreOptions()
                    .setType("file")
                    .setFormat("raw")
                    .setConfig(
                        new JsonObject()
                            .put("path", "src/test/resources/raw/logo-white-big.png")
                            .put("raw.type", "binary")
                            .put("raw.key", "logo")))
    );

    retriever.getConfiguration(ar -> {
      assertThat(ar.result()).isNotNull().isNotEmpty();
      assertThat(ar.result().getBinary("logo")).isNotEmpty();
      async.complete();
    });
  }

  @Test
  public void testWithMissingKey(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigurationRetriever.create(vertx,
        new ConfigurationRetrieverOptions()
            .addStore(
                new ConfigurationStoreOptions()
                    .setType("file")
                    .setFormat("raw")
                    .setConfig(
                        new JsonObject()
                            .put("path", "src/test/resources/raw/logo-white-big.png")
                            .put("raw.type", "binary")))
    );

    retriever.getConfiguration(ar -> {
      assertThat(ar.failed());
      assertThat(ar.cause().getMessage()).contains("raw.key");
      async.complete();
    });
  }

  @Test
  public void testWithUnrecognizedType(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigurationRetriever.create(vertx,
        new ConfigurationRetrieverOptions()
            .addStore(
                new ConfigurationStoreOptions()
                    .setType("file")
                    .setFormat("raw")
                    .setConfig(
                        new JsonObject()
                            .put("path", "src/test/resources/raw/logo-white-big.png")
                            .put("raw.key", "any")
                            .put("raw.type", "not a valid type")))
    );

    retriever.getConfiguration(ar -> {
      assertThat(ar.failed());
      assertThat(ar.cause().getMessage()).contains("raw.type");
      async.complete();
    });
  }

  @Test
  public void testWithBrokenJson(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigurationRetriever.create(vertx,
        new ConfigurationRetrieverOptions()
            .addStore(
                new ConfigurationStoreOptions()
                    .setType("file")
                    .setFormat("raw")
                    .setConfig(
                        new JsonObject()
                            .put("path", "src/test/resources/raw/username")
                            .put("raw.type", "json-object")
                            .put("raw.key", "some-json")))
    );

    retriever.getConfiguration(ar -> {
      assertThat(ar.failed());
      assertThat(ar.cause().getMessage()).contains("'admin'");
      async.complete();
    });
  }



}
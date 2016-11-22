package io.vertx.ext.configuration.yaml;

import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystemException;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.configuration.ConfigurationRetriever;
import io.vertx.ext.configuration.ConfigurationRetrieverOptions;
import io.vertx.ext.configuration.ConfigurationStoreOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.Assert.fail;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class YamlProcessorTest {

  private ConfigurationRetriever retriever;
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
  public void testEmptyYaml(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigurationRetriever.create(vertx,
        new ConfigurationRetrieverOptions().addStore(
            new ConfigurationStoreOptions()
                .setType("file")
                .setFormat("yaml")
                .setConfig(new JsonObject().put("path", "src/test/resources/empty.yaml"))));

    retriever.getConfiguration(ar -> {
      expectSuccess(ar);
      assertThat(ar.result()).isNotNull();
      assertThat(ar.result()).isEmpty();
      async.complete();
    });
  }

  private void expectSuccess(AsyncResult ar) {
    if (ar.failed()) {
      ar.cause().printStackTrace();
      fail("Failure unexpected: " + ar.cause().getMessage());
    }
    assertThat(ar.succeeded()).isTrue();
  }

  @Test
  public void testWithTextFile(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigurationRetriever.create(vertx,
        new ConfigurationRetrieverOptions().addStore(
            new ConfigurationStoreOptions()
                .setType("file")
                .setFormat("yaml")
                .setConfig(new JsonObject().put("path", "src/test/resources/some-text.txt"))));

    retriever.getConfiguration(ar -> {
      assertThat(ar.failed()).isTrue();
      assertThat(ar.cause()).isNotNull().isInstanceOf(DecodeException.class);
      async.complete();
    });
  }

  @Test
  public void testWithMissingFile(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigurationRetriever.create(vertx,
        new ConfigurationRetrieverOptions().addStore(
            new ConfigurationStoreOptions()
                .setType("file")
                .setFormat("yaml")
                .setConfig(new JsonObject().put("path", "src/test/resources/some-missing-file.yaml"))));

    retriever.getConfiguration(ar -> {
      assertThat(ar.failed()).isTrue();
      assertThat(ar.cause()).isNotNull().isInstanceOf(FileSystemException.class);
      async.complete();
    });
  }

  @Test
  public void testBasicYamlFile(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigurationRetriever.create(vertx,
        new ConfigurationRetrieverOptions().addStore(
            new ConfigurationStoreOptions()
                .setType("file")
                .setFormat("yaml")
                .setConfig(new JsonObject().put("path", "src/test/resources/basic.yaml"))));

    retriever.getConfiguration(ar -> {
      expectSuccess(ar);
      JsonObject json = ar.result();
      assertThat(json.getInteger("invoice")).isEqualTo(34843);
      assertThat(json.getString("date")).isEqualTo("2001-01-23");
      JsonObject bill = json.getJsonObject("bill-to");
      assertThat(bill).contains(entry("given", "Chris"), entry("family", "Dumars"));
      assertThat(bill.getJsonObject("address")).isNotNull().isNotEmpty();
      JsonArray products = json.getJsonArray("product");
      assertThat(products).hasSize(2);
      assertThat(products.getJsonObject(0).getFloat("price")).isEqualTo(450.0f);
      assertThat(products.getJsonObject(1).getDouble("price")).isEqualTo(2392.0);
      assertThat(json.getString("comments")).contains("Late afternoon is best. Backup contact is Nancy Billsmer @");
      async.complete();
    });
  }

  @Test
  public void testSimpleYamlConfiguration(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigurationRetriever.create(vertx,
        new ConfigurationRetrieverOptions().addStore(
            new ConfigurationStoreOptions()
                .setType("file")
                .setFormat("yaml")
                .setConfig(new JsonObject().put("path", "src/test/resources/simple.yaml"))));

    retriever.getConfiguration(ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
      }
      assertThat(ar.succeeded()).isTrue();
      JsonObject json = ar.result();
      assertThat(json.getString("key")).isEqualTo("value");

      assertThat(json.getBoolean("true")).isTrue();
      assertThat(json.getBoolean("false")).isFalse();

      assertThat(json.getString("missing")).isNull();

      assertThat(json.getInteger("int")).isEqualTo(5);
      assertThat(json.getDouble("float")).isEqualTo(25.3);

      assertThat(json.getJsonArray("array").size()).isEqualTo(3);
      assertThat(json.getJsonArray("array").contains(1)).isTrue();
      assertThat(json.getJsonArray("array").contains(2)).isTrue();
      assertThat(json.getJsonArray("array").contains(3)).isTrue();

      assertThat(json.getJsonObject("sub").getString("foo")).isEqualTo("bar");

      async.complete();
    });
  }

  @Test
  public void testStructures(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigurationRetriever.create(vertx,
        new ConfigurationRetrieverOptions().addStore(
            new ConfigurationStoreOptions()
                .setType("file")
                .setFormat("yaml")
                .setConfig(new JsonObject().put("path", "src/test/resources/structure.yaml"))));

    retriever.getConfiguration(ar -> {
      expectSuccess(ar);
      JsonObject json = ar.result();
      // Lists
      assertThat(json.getJsonArray("movies")).containsExactly("Casablanca", "V for Vendetta");
      assertThat(json.getJsonArray("shopping")).containsExactly("milk", "pumpkin pie", "eggs", "juice");

      // Blocks
      assertThat(json.getJsonObject("blockA")).containsExactly(entry("name", "John Smith"), entry("age", 33));
      assertThat(json.getJsonObject("blockB")).containsExactly(entry("name", "John Smith"), entry("age", 33));

      // New lines preserved
      assertThat(json.getString("newLines-preserved")).contains("\n", "\"");

      // New line folded
      assertThat(json.getString("newLines-folded"))
          .contains("Wrapped text will be folded into a single paragraph")
          .contains("\nBlank lines denote");

      // Anchors
      // TODO Anchors are not supported yet.
//      System.out.println(json.getJsonArray("anchors").encodePrettily());
//      JsonArray anchors = json.getJsonArray("anchors");
//      assertThat(anchors.getJsonObject(0).getJsonObject("step")).contains(entry("instrument", "Lasik 2000"), entry
//              ("spotSize", "1mm"),
//          entry("pulseDuration", 12));
//      assertThat(anchors.getJsonObject(1).getJsonObject("step")).contains(entry("instrument", "Lasik 2000"), entry("spotSize", "2mm"),
//          entry("pulseDuration", 10));
//      assertThat(anchors.getJsonObject(3).getJsonObject("step")).contains(entry("instrument", "Lasik 2000"), entry("spotSize", "1mm"),
//          entry("pulseDuration", 12));
//      assertThat(anchors.getJsonObject(4).getJsonObject("step")).contains(entry("instrument", "Lasik 2000"), entry("spotSize", "2mm"),
//          entry("pulseDuration", 10));
//      assertThat(anchors.getJsonObject(5).getJsonObject("step")).contains(entry("instrument", "Lasik 2000"), entry("spotSize", "2mm"),
//          entry("pulseDuration", 12));
//      assertThat(anchors.getJsonObject(6).getJsonObject("step")).contains(entry("instrument", "Lasik 2000"), entry("spotSize", "2mm"),
//          entry("pulseDuration", 10));

      // Binary
      assertThat(json.getBinary("picture")).isNotEmpty();

      async.complete();
    });
  }
}
package io.vertx.ext.configuration.impl.spi;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.configuration.ConfigurationService;
import io.vertx.ext.configuration.ConfigurationServiceOptions;
import io.vertx.ext.configuration.ConfigurationStoreOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Check the behavior of the {@link DirectoryConfigurationStore}
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class DirectoryConfigurationStoreTest extends ConfigurationStoreTestBase {

  @Before
  public void init() {
    factory = new DirectoryConfigurationStoreFactory();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWithMissingPathInConf() {
    store = factory.create(vertx, new JsonObject().put("no-path", ""));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWithMissingFileSets() {
    store = factory.create(vertx, new JsonObject().put("path",
        "src/test/resources"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWithMissingPatternInAFileSet() {
    store = factory.create(vertx, new JsonObject().put("path", "src/test/resources")
        .put("filesets", new JsonArray().add(new JsonObject().put("format", "properties"))));
  }

  @Test
  public void testName() {
    assertThat(factory.name()).isNotNull().isEqualTo("directory");
  }

  @Test
  public void testWithNonExistingPath(TestContext tc) {
    Async async = tc.async();
    store = factory.create(vertx, new JsonObject().put("path", "src/test/missing")
        .put("filesets", new JsonArray().add(new JsonObject().put("pattern", "*.json"))));
    store.get(ar -> {
      assertThat(ar.succeeded()).isTrue();
      assertThat(ar.result()).isEqualTo(Buffer.buffer("{}"));
      async.complete();
    });
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWithAPathThatIsAFile() {
    store = factory.create(vertx, new JsonObject().put("path", "src/test/resources/file/regular.json")
        .put("filesets", new JsonArray().add(new JsonObject().put("pattern", "*.json"))));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWhenTheFormatIsUnknown() {
    store = factory.create(vertx, new JsonObject().put("path", "src/test/resources/file")
        .put("filesets", new JsonArray().add(new JsonObject().put("pattern", "*.json").put("format", "bad"))));
  }

  @Test
  public void testLoadingASingleJsonFile(TestContext context) {
    Async async = context.async();
    service = ConfigurationService.create(vertx, new ConfigurationServiceOptions()
        .addStore(new ConfigurationStoreOptions()
            .setType("directory")
            .setConfig(new JsonObject().put("path", "src/test/resources")
                .put("filesets", new JsonArray().add(new JsonObject().put("pattern", "file/reg*.json"))))));
    service.getConfiguration(ar -> {
      ConfigurationChecker.check(ar);
      async.complete();
    });
  }

  @Test
  public void testLoadingASinglePropertiesFile(TestContext context) {
    Async async = context.async();
    service = ConfigurationService.create(vertx, new ConfigurationServiceOptions()
        .addStore(new ConfigurationStoreOptions()
            .setType("directory")
            .setConfig(new JsonObject().put("path", "src/test/resources")
                .put("filesets", new JsonArray().add(new JsonObject()
                    .put("format", "properties")
                    .put("pattern", "**/reg*.properties"))))));

    service.getConfiguration(ar -> {
      assertThat(ar.succeeded()).isTrue();
      JsonObject json = ar.result();
      assertThat(json.getString("key")).isEqualTo("value");
      assertThat(json.getBoolean("true")).isTrue();
      assertThat(json.getBoolean("false")).isFalse();
      assertThat(json.getString("missing")).isNull();
      assertThat(json.getInteger("int")).isEqualTo(5);
      assertThat(json.getDouble("float")).isEqualTo(25.3);
      async.complete();
    });
  }


  @Test
  public void testWhenNoFileMatch(TestContext context) {
    Async async = context.async();
    service = ConfigurationService.create(vertx, new ConfigurationServiceOptions()
        .addStore(new ConfigurationStoreOptions()
            .setType("directory")
            .setConfig(new JsonObject().put("path", "src/test/resources")
                .put("filesets", new JsonArray().add(new JsonObject()
                    .put("format", "properties")
                    .put("pattern", "**/reg*.stuff"))))));

    service.getConfiguration(ar -> {
      assertThat(ar.succeeded()).isTrue();
      JsonObject json = ar.result();
      assertThat(json.isEmpty());
      async.complete();
    });
  }

  @Test
  public void testWith2FileSetsAndNoIntersection(TestContext context) {
    Async async = context.async();
    service = ConfigurationService.create(vertx, new ConfigurationServiceOptions()
        .addStore(new ConfigurationStoreOptions()
            .setType("directory")
            .setConfig(new JsonObject().put("path", "src/test/resources")
                .put("filesets", new JsonArray()
                    .add(new JsonObject().put("pattern", "file/reg*.json"))
                    .add(new JsonObject().put("pattern", "dir/a.*son"))
                ))));
    service.getConfiguration(ar -> {
      ConfigurationChecker.check(ar);
      assertThat(ar.result().getString("a.name")).isEqualTo("A");
      async.complete();
    });
  }

  @Test
  public void testWith2FileSetsAndWithIntersection(TestContext context) {
    Async async = context.async();
    service = ConfigurationService.create(vertx, new ConfigurationServiceOptions()
        .addStore(new ConfigurationStoreOptions()
            .setType("directory")
            .setConfig(new JsonObject().put("path", "src/test/resources")
                .put("filesets", new JsonArray()
                    .add(new JsonObject().put("pattern", "dir/b.json"))
                    .add(new JsonObject().put("pattern", "dir/a.*son"))
                ))));
    service.getConfiguration(ar -> {
      assertThat(ar.result().getString("a.name")).isEqualTo("A");
      assertThat(ar.result().getString("b.name")).isEqualTo("B");
      assertThat(ar.result().getString("conflict")).isEqualTo("A");
      async.complete();
    });
  }

  @Test
  public void testWith2FileSetsAndWithIntersectionReversed(TestContext context) {
    Async async = context.async();
    service = ConfigurationService.create(vertx, new ConfigurationServiceOptions()
        .addStore(new ConfigurationStoreOptions()
            .setType("directory")
            .setConfig(new JsonObject().put("path", "src/test/resources")
                .put("filesets", new JsonArray()
                    .add(new JsonObject().put("pattern", "dir/a.*son"))
                    .add(new JsonObject().put("pattern", "dir/b.json"))
                ))));
    service.getConfiguration(ar -> {
      assertThat(ar.result().getString("a.name")).isEqualTo("A");
      assertThat(ar.result().getString("b.name")).isEqualTo("B");
      assertThat(ar.result().getString("conflict")).isEqualTo("B");
      async.complete();
    });
  }

  @Test
  public void testWithAFileSetMatching2FilesWithConflict(TestContext context) {
    Async async = context.async();
    service = ConfigurationService.create(vertx, new ConfigurationServiceOptions()
        .addStore(new ConfigurationStoreOptions()
            .setType("directory")
            .setConfig(new JsonObject().put("path", "src/test/resources")
                .put("filesets", new JsonArray()
                    .add(new JsonObject().put("pattern", "dir/?.json"))
                ))));
    service.getConfiguration(ar -> {
      assertThat(ar.result().getString("b.name")).isEqualTo("B");
      assertThat(ar.result().getString("a.name")).isEqualTo("A");
      // Alphabetical order, so B is last.
      assertThat(ar.result().getString("conflict")).isEqualTo("B");
      async.complete();
    });
  }

  @Test
  public void testWithAFileSetMatching2FilesWithoutConflict(TestContext context) {
    Async async = context.async();
    service = ConfigurationService.create(vertx, new ConfigurationServiceOptions()
        .addStore(new ConfigurationStoreOptions()
            .setType("directory")
            .setConfig(new JsonObject().put("path", "src/test/resources")
                .put("filesets", new JsonArray()
                    .add(new JsonObject().put("pattern", "dir/a?.json"))
                ))));
    service.getConfiguration(ar -> {
      assertThat(ar.result().getString("b.name")).isNull();
      assertThat(ar.result().getString("a.name")).isEqualTo("A");
      assertThat(ar.result().getString("c.name")).isEqualTo("C");
      assertThat(ar.result().getString("conflict")).isEqualTo("A");
      async.complete();
    });
  }

  @Test
  public void testWithAFileSetMatching2FilesOneNotBeingAJsonFile(TestContext context) {
    Async async = context.async();
    service = ConfigurationService.create(vertx, new ConfigurationServiceOptions()
        .addStore(new ConfigurationStoreOptions()
            .setType("directory")
            .setConfig(new JsonObject().put("path", "src/test/resources")
                .put("filesets", new JsonArray()
                    .add(new JsonObject().put("pattern", "dir/a?*.json"))
                ))));
    service.getConfiguration(ar -> {
      assertThat(ar.failed());
      assertThat(ar.cause()).isInstanceOf(DecodeException.class);
      async.complete();
    });
  }


}
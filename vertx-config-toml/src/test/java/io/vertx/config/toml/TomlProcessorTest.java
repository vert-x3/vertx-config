package io.vertx.config.toml;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystemException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Objects;


@RunWith(VertxUnitRunner.class)
public final class TomlProcessorTest {

  private static final String EMPTY_FILE_PATH = "src/test/resources/empty.toml";
  private static final String NON_EXISTENT_PATH = "src/test/resources/does-not-exist.toml";
  private static final String EXAMPLE_PATH = "src/test/resources/example.toml";
  private static final JsonObject EXAMPLE_JSON = loadJson("src/test/resources/example.json");
  private static final String COMPLEX_EXAMPLE_PATH = "src/test/resources/complex-example.toml";
  private static final String BAD_FILE_PATH = "src/test/resources/bad.toml";

  @Rule
  public RunTestOnContext contextRule = new RunTestOnContext();

  private Vertx vertx;

  @Before
  public void setUp(TestContext context) throws Exception {
    vertx = contextRule.vertx();
  }

  @Test
  public void works_for_empty_file(TestContext context) {
    Async async = context.async();
    ConfigStoreOptions storeOptions = createFileStoreOptions(EMPTY_FILE_PATH);

    ConfigRetrieverOptions retrieverOptions =
            new ConfigRetrieverOptions().addStore(storeOptions);
    ConfigRetriever retriever = ConfigRetriever.create(vertx, retrieverOptions);

    retriever.getConfig(event -> {
      if (event.succeeded()) {
        JsonObject result = event.result();
        context.assertEquals(
                new JsonObject(),
                result,
                "should have returned empty JSON"
        );
      } else {
        context.fail(event.cause());
      }
      async.countDown();
    });
  }

  @Test
  public void works_for_example_file(TestContext context) {
    Async async = context.async();
    ConfigStoreOptions storeOptions = createFileStoreOptions(EXAMPLE_PATH);

    ConfigRetrieverOptions retrieverOptions =
            new ConfigRetrieverOptions().addStore(storeOptions);
    ConfigRetriever retriever = ConfigRetriever.create(vertx, retrieverOptions);

    retriever.getConfig(event -> {
      if (event.succeeded()) {
        JsonObject result = event.result();
        context.assertEquals(
                EXAMPLE_JSON,
                result,
                "should have returned empty JSON"
        );
      } else {
        context.fail(event.cause());
      }
      async.countDown();
    });
  }

  @Test
  public void works_for_complex_example_file(TestContext context) {
    Async async = context.async();
    ConfigStoreOptions storeOptions = createFileStoreOptions(COMPLEX_EXAMPLE_PATH);

    ConfigRetrieverOptions retrieverOptions =
            new ConfigRetrieverOptions().addStore(storeOptions);
    ConfigRetriever retriever = ConfigRetriever.create(vertx, retrieverOptions);

    retriever.getConfig(event -> {
      if (event.succeeded()) {
        JsonObject result = event.result();
        context.assertNotNull(result);
      } else {
        context.fail(event.cause());
      }
      async.countDown();
    });
  }

  @Test
  public void fails_for_non_existent_file(TestContext context) {
    Async async = context.async();
    ConfigStoreOptions storeOptions = createFileStoreOptions(NON_EXISTENT_PATH);

    ConfigRetrieverOptions retrieverOptions =
            new ConfigRetrieverOptions().addStore(storeOptions);
    ConfigRetriever retriever = ConfigRetriever.create(vertx, retrieverOptions);

    retriever.getConfig(event -> {
      context.assertTrue(event.failed(), "should have failed");
      if (event.failed()) {
        Throwable cause = event.cause();
        if (cause instanceof FileSystemException) {
          FileSystemException cast = (FileSystemException) cause;
          context.assertTrue(
                  cast.getCause() instanceof NoSuchFileException,
                  "wrong cause"
          );
        } else {
          context.fail(cause);
        }
      }
      async.countDown();
    });
  }

  @Test
  public void fails_for_bad_file(TestContext context) {
    Async async = context.async();
    ConfigStoreOptions storeOptions = createFileStoreOptions(BAD_FILE_PATH);

    ConfigRetrieverOptions retrieverOptions =
            new ConfigRetrieverOptions().addStore(storeOptions);
    ConfigRetriever retriever = ConfigRetriever.create(vertx, retrieverOptions);

    retriever.getConfig(event -> {
      context.assertTrue(event.failed(), "should have failed");
      if (event.failed()) {
        Throwable cause = event.cause();
        context.assertTrue(
                cause instanceof IllegalStateException,
                "wrong cause"
        );
      }
      async.countDown();
    });
  }


  private static ConfigStoreOptions createFileStoreOptions(
          String path
  ) {
    Objects.requireNonNull(path);
    return new ConfigStoreOptions()
            .setFormat("toml")
            .setType("file")
            .setConfig(new JsonObject().put("path", path));
  }

  private static JsonObject loadJson(String path) {
    Objects.requireNonNull(path);
    byte[] bytes;
    try {
      bytes = Files.readAllBytes(Paths.get(path));
    } catch (IOException e) {
      throw new IllegalArgumentException(
              "Unable to load data from path: " + path,
              e
      );
    }
    Buffer buffer = Buffer.buffer(bytes);
    return new JsonObject(buffer);
  }
}

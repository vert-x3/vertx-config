package io.vertx.config;

import io.vertx.config.impl.spi.ConfigurationChecker;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RepeatRule;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class ConfigurationStreamTest {

  private Vertx vertx;
  private ConfigurationRetriever retriever;
  private boolean doClose = true;

  @Rule
  public RepeatRule repeatRule = new RepeatRule();

  @Before
  public void setUp(TestContext tc) {
    vertx = Vertx.vertx();
    vertx.exceptionHandler(tc.exceptionHandler());

    System.setProperty("foo", "bar");
  }

  @After
  public void tearDown() {
    if (doClose) {
      retriever.close();
    }
    vertx.close();
    System.clearProperty("key");
    System.clearProperty("foo");
  }

  private static ConfigurationRetrieverOptions addStores(ConfigurationRetrieverOptions options) {
    return options
        .addStore(
            new ConfigurationStoreOptions()
                .setType("file")
                .setConfig(new JsonObject().put("path", "src/test/resources/file/regular.json")))
        .addStore(
            new ConfigurationStoreOptions()
                .setType("sys")
                .setConfig(new JsonObject().put("cache", false)));
  }

  @Test
  public void testRetrievingTheConfiguration(TestContext tc) {
    retriever = ConfigurationRetriever.create(vertx,
        addStores(new ConfigurationRetrieverOptions()));
    Async async = tc.async();
    retriever.configurationStream()
        .handler(conf -> {
          ConfigurationChecker.check(conf);
          assertThat(conf.getString("foo")).isEqualToIgnoringCase("bar");
          ConfigurationChecker.check(retriever.getCachedConfiguration());
          async.complete();
        });
  }

  @Test
  public void testRetrievingTheConfigurationAndClose(TestContext tc) {
    retriever = ConfigurationRetriever.create(vertx,
        addStores(new ConfigurationRetrieverOptions()));
    Async async = tc.async();
    doClose = false;
    retriever.configurationStream()
        .endHandler(v -> async.complete())
        .handler(conf -> {
          ConfigurationChecker.check(conf);
          assertThat(conf.getString("foo")).isEqualToIgnoringCase("bar");
          ConfigurationChecker.check(retriever.getCachedConfiguration());
          retriever.close();
        });
  }

  @Test
  public void testPauseResumeCycles(TestContext tc) {
    retriever = ConfigurationRetriever.create(vertx,
        addStores(new ConfigurationRetrieverOptions()));
    Async async = tc.async();
    AtomicInteger steps = new AtomicInteger();
    retriever.configurationStream()
        .handler(conf -> {
          if (steps.get() == 0) {
            assertThat(conf.getString("foo")).isEqualToIgnoringCase("bar");
            retriever.configurationStream().pause();
            System.setProperty("foo", "bar2");
            retriever.configurationStream().resume();
            steps.incrementAndGet();
          } else if (steps.get() == 1) {
            assertThat(conf.getString("foo")).isEqualToIgnoringCase("bar2");
            async.complete();
          }
        });
  }

}
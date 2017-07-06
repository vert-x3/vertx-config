package io.vertx.config.consul;

import com.pszymczyk.consul.ConsulProcess;
import com.pszymczyk.consul.ConsulStarterBuilder;
import com.pszymczyk.consul.LogLevel;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.consul.ConsulClient;
import io.vertx.ext.consul.ConsulClientOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Check the behavior of {@link ConsulConfigStore}.
 *
 * @author <a href="mailto:ruslan.sennov@gmail.com">Ruslan Sennov</a>
 */
@RunWith(VertxUnitRunner.class)
public class ConsulConfigStoreTest {

  private static final String CONSUL_VERSION = "0.7.5";
  private static ConsulProcess consulProcess;

  @BeforeClass
  public static void consulUp() {
    consulProcess = ConsulStarterBuilder.consulStarter()
      .withLogLevel(LogLevel.ERR)
      .withConsulVersion(CONSUL_VERSION)
      .build()
      .start();
  }

  @AfterClass
  public static void consulDown() {
    consulProcess.close();
  }

  private ConfigRetriever retriever;
  private Vertx vertx;
  private ConsulClient client;

  @Before
  public void setUp(TestContext tc) throws Exception {
    vertx = Vertx.vertx();
    vertx.exceptionHandler(tc.exceptionHandler());

    client = ConsulClient.create(vertx, new ConsulClientOptions().setPort(consulProcess.getHttpPort()));
  }

  @After
  public void tearDown(TestContext tc) {
    retriever.close();
    client.close();
    vertx.close(tc.asyncAssertSuccess());
  }

  @Test
  public void getEmptyConfig(TestContext tc) {
    Async async = tc.async();
    createRetriever();
    retriever.getConfig(json -> {
      tc.assertTrue(json.succeeded());
      tc.assertTrue(json.result().isEmpty());
      async.complete();
    });
  }

  @Test
  public void getSimpleConfig(TestContext tc) {
    Async async = tc.async();
    createRetriever();
    client.putValue("foo/bar", "value", ar -> {
      tc.assertTrue(ar.succeeded());
      retriever.getConfig(json2 -> {
        assertThat(json2.succeeded()).isTrue();
        JsonObject config2 = json2.result();
        tc.assertTrue(!config2.isEmpty());
        tc.assertEquals(config2.getString("bar"), "value");
        client.deleteValues("foo", h -> async.complete());
      });
    });
  }

  @Test
  public void listenConfigChange(TestContext tc) {
    Async async = tc.async();
    createRetriever();
    client.putValue("foo/bar", "value", ar -> {
      tc.assertTrue(ar.succeeded());
      retriever.getConfig(init ->
        retriever.listen(change -> {
          JsonObject prev = change.getPreviousConfiguration();
          tc.assertTrue(!prev.isEmpty());
          tc.assertEquals(prev.getString("bar"), "value");
          JsonObject next = change.getNewConfiguration();
          tc.assertTrue(!next.isEmpty());
          tc.assertEquals(next.getString("bar"), "new_value");
          client.deleteValues("foo", h -> async.complete());
        }));
      client.putValue("foo/bar", "new_value", ignore -> {});
    });
  }

  private void createRetriever() {
    retriever = ConfigRetriever.create(vertx,
      new ConfigRetrieverOptions().addStore(
        new ConfigStoreOptions()
          .setType("consul")
          .setConfig(new JsonObject()
            .put("port", consulProcess.getHttpPort())
            .put("prefix", "foo"))));
  }
}

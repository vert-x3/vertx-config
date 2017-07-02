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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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

  private ConfigRetriever retriever;
  private Vertx vertx;
  private ConsulProcess consul;
  private ConsulClient client;

  @Before
  public void setUp(TestContext tc) throws Exception {
    vertx = Vertx.vertx();
    vertx.exceptionHandler(tc.exceptionHandler());

    consul = ConsulStarterBuilder.consulStarter()
      .withLogLevel(LogLevel.ERR)
      .withConsulVersion(CONSUL_VERSION)
      .build()
      .start();
    client = ConsulClient.create(vertx, new ConsulClientOptions().setPort(consul.getHttpPort()));
  }

  @After
  public void tearDown(TestContext tc) throws IOException {
    retriever.close();
    consul.close();
    client.close();
    vertx.close(tc.asyncAssertSuccess());
  }

  @Test
  public void getConfigurationFromConsul(TestContext tc) throws Exception {
    Async async = tc.async();
    retriever = ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions().addStore(
            new ConfigStoreOptions()
                .setType("consul")
                .setConfig(new JsonObject()
                    .put("defaultPort", consul.getHttpPort())
                    .put("prefix", "foo"))));


    retriever.getConfig(json -> {
      assertThat(json.succeeded()).isTrue();
      JsonObject config = json.result();
      tc.assertTrue(config.isEmpty());

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
    });

  }
}

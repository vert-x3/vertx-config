package io.vertx.config.redis;

import io.vertx.config.ConfigurationRetriever;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.config.ConfigurationRetrieverOptions;
import io.vertx.config.ConfigurationStoreOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import redis.embedded.RedisServer;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Check the behavior of {@link RedisConfigurationStore}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class RedisConfigurationStoreTest {

  private ConfigurationRetriever retriever;
  private Vertx vertx;
  private RedisServer redisServer;
  private RedisClient testRedisClient;


  @Before
  public void setUp(TestContext tc) throws IOException {
    vertx = Vertx.vertx();
    vertx.exceptionHandler(tc.exceptionHandler());

    redisServer = new RedisServer(6379);
    redisServer.start();

    testRedisClient = RedisClient.create(vertx,
        new RedisOptions().setHost("localhost").setPort(6379));
  }


  @After
  public void tearDown(TestContext tc) {
    retriever.close();
    testRedisClient.close(tc.asyncAssertSuccess());
    vertx.close(tc.asyncAssertSuccess());
    redisServer.stop();
  }

  @Test
  public void getWithDefaultKey(TestContext tc) throws Exception {
    Async async = tc.async();
    retriever = ConfigurationRetriever.create(vertx,
        new ConfigurationRetrieverOptions().addStore(
            new ConfigurationStoreOptions()
                .setType("redis")
                .setConfig(new JsonObject()
                    .put("host", "localhost")
                    .put("port", 6379))));


    retriever.getConfiguration(json -> {
      assertThat(json.succeeded()).isTrue();
      JsonObject config = json.result();
      tc.assertTrue(config.isEmpty());

      writeSomeConf("configuration", ar -> {
        tc.assertTrue(ar.succeeded());

        retriever.getConfiguration(json2 -> {
          assertThat(json2.succeeded()).isTrue();
          JsonObject config2 = json2.result();
          tc.assertTrue(!config2.isEmpty());
          tc.assertEquals(config2.getString("some-key"), "some-value");
          async.complete();
        });
      });
    });

  }

  @Test
  public void getWithConfiguredKey(TestContext tc) throws Exception {
    Async async = tc.async();
    retriever = ConfigurationRetriever.create(vertx,
        new ConfigurationRetrieverOptions().addStore(
            new ConfigurationStoreOptions()
                .setType("redis")
                .setConfig(new JsonObject()
                    .put("key", "my-config")
                    .put("host", "localhost")
                    .put("port", 6379))));


    retriever.getConfiguration(json -> {
      assertThat(json.succeeded()).isTrue();
      JsonObject config = json.result();
      tc.assertTrue(config.isEmpty());

      writeSomeConf("my-config", ar -> {
        tc.assertTrue(ar.succeeded());

        retriever.getConfiguration(json2 -> {
          assertThat(json2.succeeded()).isTrue();
          JsonObject config2 = json2.result();
          tc.assertTrue(!config2.isEmpty());
          tc.assertEquals(config2.getString("some-key"), "some-value");
          async.complete();
        });
      });
    });

  }

  private void writeSomeConf(String key, Handler<AsyncResult<Void>> handler) {
    JsonObject conf = new JsonObject().put("some-key", "some-value");
    testRedisClient.hmset(key, conf, ar -> {
      if (ar.succeeded()) {
        handler.handle(Future.succeededFuture());
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

}
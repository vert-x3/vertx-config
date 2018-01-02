/*
 * Copyright (c) 2014 Red Hat, Inc. and others
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package io.vertx.config.redis;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.config.ConfigStoreOptions;
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
 * Check the behavior of {@link RedisConfigStore}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class RedisConfigStoreTest {

  private ConfigRetriever retriever;
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
    retriever = ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions().addStore(
            new ConfigStoreOptions()
                .setType("redis")
                .setConfig(new JsonObject()
                    .put("host", "localhost")
                    .put("port", 6379))));


    retriever.getConfig(json -> {
      assertThat(json.succeeded()).isTrue();
      JsonObject config = json.result();
      tc.assertTrue(config.isEmpty());

      writeSomeConf("configuration", ar -> {
        tc.assertTrue(ar.succeeded());

        retriever.getConfig(json2 -> {
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
    retriever = ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions().addStore(
            new ConfigStoreOptions()
                .setType("redis")
                .setConfig(new JsonObject()
                    .put("key", "my-config")
                    .put("host", "localhost")
                    .put("port", 6379))));


    retriever.getConfig(json -> {
      assertThat(json.succeeded()).isTrue();
      JsonObject config = json.result();
      tc.assertTrue(config.isEmpty());

      writeSomeConf("my-config", ar -> {
        tc.assertTrue(ar.succeeded());

        retriever.getConfig(json2 -> {
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

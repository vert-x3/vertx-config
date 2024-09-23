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

package io.vertx.config.redis.tests;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.config.redis.RedisConfigStore;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisOptions;
import org.junit.*;
import org.junit.runner.RunWith;
import org.testcontainers.containers.GenericContainer;

import java.util.Arrays;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Check the behavior of {@link RedisConfigStore}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class Redis6ConfigStoreTest {

  @Rule
  public final RunTestOnContext rule = new RunTestOnContext();

  @ClassRule
  public static final GenericContainer<?> redis = new GenericContainer<>("redis:6.2")
    .withExposedPorts(6379);

  private Redis client;
  private ConfigRetriever retriever;

  @Before
  public void before(TestContext should) {
    final Async before = should.async();

    client = Redis.createClient(
      rule.vertx(),
      new RedisOptions().setConnectionString("redis://" + redis.getContainerIpAddress() + ":" + redis.getFirstMappedPort() + "?client=tester"));

    client.connect().onComplete(onConnect -> {
      should.assertTrue(onConnect.succeeded());
      onConnect.result().close();
      before.complete();
    });
  }

  @After
  public void after() {
    client.close();
    if (retriever != null) {
      retriever.close();
    }
  }

  @Test
  public void getWithDefaultKey(TestContext tc) throws Exception {
    Async async = tc.async();
    retriever = ConfigRetriever.create(rule.vertx(),
        new ConfigRetrieverOptions().addStore(
            new ConfigStoreOptions()
                .setType("redis")
                .setConfig(new JsonObject()
                  .put("endpoints", new JsonArray().add("redis://" + redis.getContainerIpAddress() + ":" + redis.getFirstMappedPort())))));
    retriever.getConfig().onComplete(json -> {
      assertThat(json.succeeded()).isTrue();
      JsonObject config = json.result();
      tc.assertTrue(config.isEmpty());
      writeSomeConf("configuration", ar -> {
        tc.assertTrue(ar.succeeded());
        retriever.getConfig().onComplete(json2 -> {
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
    retriever = ConfigRetriever.create(rule.vertx(),
        new ConfigRetrieverOptions().addStore(
            new ConfigStoreOptions()
                .setType("redis")
                .setConfig(new JsonObject()
                  .put("key", "my-config")
                  .put("endpoints", new JsonArray().add("redis://" + redis.getContainerIpAddress() + ":" + redis.getFirstMappedPort())))));


    retriever.getConfig().onComplete(json -> {
      assertThat(json.succeeded()).isTrue();
      JsonObject config = json.result();
      tc.assertTrue(config.isEmpty());

      writeSomeConf("my-config", ar -> {
        tc.assertTrue(ar.succeeded());

        retriever.getConfig().onComplete(json2 -> {
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
    RedisAPI api = RedisAPI.api(client);
    api.hmset(Arrays.asList(key, "some-key", "some-value")).onComplete(ar -> {
      if (ar.succeeded()) {
        handler.handle(Future.succeededFuture());
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }
}

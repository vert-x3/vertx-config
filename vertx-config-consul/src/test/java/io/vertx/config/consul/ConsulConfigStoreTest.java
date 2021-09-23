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

package io.vertx.config.consul;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.consul.ConsulClient;
import io.vertx.ext.consul.ConsulClientOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Check the behavior of {@link ConsulConfigStore}.
 *
 * @author <a href="mailto:ruslan.sennov@gmail.com">Ruslan Sennov</a>
 */
@RunWith(VertxUnitRunner.class)
public class ConsulConfigStoreTest {

  private static final String CONSUL_VERSION = "1.0.7";

  static GenericContainer<?> CONSUL_CONTAINER;

  @BeforeClass
  public static void start() {
    CONSUL_CONTAINER = new GenericContainer<>(DockerImageName.parse("consul:" + CONSUL_VERSION))
      .withExposedPorts(8500);

    CONSUL_CONTAINER.start();
    CONSUL_CONTAINER.waitingFor(Wait.forLogMessage("Synced node info", 1));
    CONSUL_CONTAINER.followOutput(frame -> {
      System.out.print("CONSUL: " + frame.getUtf8String());
    });

  }

  @AfterClass
  public static void stop() {
    if (CONSUL_CONTAINER != null) {
      GenericContainer<?> container = CONSUL_CONTAINER;
      CONSUL_CONTAINER = null;
      container.stop();
    }
  }

  private ConfigRetriever retriever;
  private Vertx vertx;
  private ConsulClient client;

  @Before
  public void setUp(TestContext tc) throws Exception {
    vertx = Vertx.vertx();
    vertx.exceptionHandler(tc.exceptionHandler());

    client = ConsulClient.create(vertx, new ConsulClientOptions().setPort(CONSUL_CONTAINER.getMappedPort(8500)));
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
    createRetriever();
    client.putValue("foo/bar", "value", tc.asyncAssertSuccess(v -> {
      retriever.getConfig(tc.asyncAssertSuccess(v2 -> {
        retriever.listen(change -> {
          JsonObject prev = change.getPreviousConfiguration();
          tc.assertTrue(!prev.isEmpty());
          tc.assertEquals(prev.getString("bar"), "value");
          JsonObject next = change.getNewConfiguration();
          tc.assertTrue(!next.isEmpty());
          tc.assertEquals(next.getString("bar"), "new_value");
          client.deleteValues("foo", tc.asyncAssertSuccess());
        });
        client.putValue("foo/bar", "new_value", ignore -> {});
      }));
    }));
  }

  @Test
  public void testNotRaw(TestContext tc) {
    createRetriever(false);
    String testStr = "{\"str\": \"bar\", \"double\": 1.2, \"bool1\": true, " +
      "\"bool0\": false, \"int\": 1234, \"long\": 9223372036854775807, " +
      "\"obj\": {\"int\": -321}, \"arr\": [\"1\", 2, false]}";
    client.putValue("foo/bar", testStr, tc.asyncAssertSuccess(v -> {
      retriever.getConfig(tc.asyncAssertSuccess(result -> {
        tc.assertFalse(result.isEmpty());
        JsonObject bar = result.getJsonObject("bar");
        tc.assertEquals("bar", bar.getString("str"));
        tc.assertTrue(1.2D - bar.getDouble("double") < 0.001);
        tc.assertEquals(1234, bar.getInteger("int"));
        tc.assertEquals(9223372036854775807L, bar.getLong("long"));
        tc.assertFalse(bar.getBoolean("bool0"));
        tc.assertTrue(bar.getBoolean("bool1"));
        tc.assertEquals(new JsonObject().put("int", -321), bar.getJsonObject("obj"));
        tc.assertEquals(new JsonArray().add("1").add(2).add(false), bar.getJsonArray("arr"));
        client.deleteValues("foo", tc.asyncAssertSuccess());
      }));
    }));
  }

  private void createRetriever() {
    createRetriever(true);
  }

  private void createRetriever(boolean raw) {
    retriever = ConfigRetriever.create(vertx,
      new ConfigRetrieverOptions().addStore(
        new ConfigStoreOptions()
          .setType("consul")
          .setConfig(new JsonObject()
            .put("port", CONSUL_CONTAINER.getMappedPort(8500))
            .put("prefix", "foo")
            .put("raw-data", raw))));
  }
}

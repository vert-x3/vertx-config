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

package io.vertx.config.zookeeper.tests;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.zookeeper.ZookeeperConfigStore;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.KeeperException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Check the behavior of {@link ZookeeperConfigStore}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class ZookeeperConfigStoreTest {

  private ConfigRetriever retriever;
  private Vertx vertx;
  private TestingServer server;
  private CuratorFramework client;


  @Before
  public void setUp(TestContext tc) throws Exception {
    server = new TestingServer(2181);
    client = CuratorFrameworkFactory.newClient(server.getConnectString(), new RetryOneTime(2000));
    client.start();

    vertx = Vertx.vertx();
    vertx.exceptionHandler(tc.exceptionHandler());
  }


  @After
  public void tearDown(TestContext tc) throws IOException {
    retriever.close();
    client.close();
    vertx.close().onComplete(tc.asyncAssertSuccess());
    server.close();
  }

  @Test
  public void getConfigurationFromZookeeper(TestContext tc) throws Exception {
    Async async = tc.async();
    retriever = ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions().addStore(
            new ConfigStoreOptions()
                .setType("zookeeper")
                .setConfig(new JsonObject()
                    .put("connection", server.getConnectString())
                    .put("path", "/config/A"))));


    retriever.getConfig().onComplete(json -> {
      assertThat(json.succeeded()).isTrue();
      JsonObject config = json.result();
      tc.assertTrue(config.isEmpty());

      writeSomeConf("/config/A", true, ar -> {
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
  public void testUpdateAndRemovalOfConfiguration(TestContext tc) throws Exception {
    Async async = tc.async();

    writeSomeConf("/config/A", true, ar -> {
      retriever = ConfigRetriever.create(vertx,
          new ConfigRetrieverOptions().addStore(
              new ConfigStoreOptions()
                  .setType("zookeeper")
                  .setConfig(new JsonObject()
                      .put("connection", server.getConnectString())
                      .put("path", "/config/A"))));

      retriever.getConfig().onComplete(json2 -> {
        assertThat(json2.succeeded()).isTrue();
        JsonObject config2 = json2.result();
        tc.assertTrue(!config2.isEmpty());
        tc.assertEquals(config2.getString("some-key"), "some-value");

        // Update the conf
        writeSomeConf("/config/A", false, update -> {
          retriever.getConfig().onComplete(json3 -> {
            assertThat(json3.succeeded()).isTrue();
            JsonObject config3 = json3.result();
            tc.assertTrue(!config3.isEmpty());
            tc.assertEquals(config3.getString("some-key"), "some-value-2");

            // Delete
            delete("/config/A", deletion -> {
              retriever.getConfig().onComplete(json4 -> {
                assertThat(json4.succeeded()).isTrue();
                JsonObject config4 = json4.result();
                tc.assertTrue(config4.isEmpty());
                async.complete();
              });
            });
          });
        });
      });
    });
  }

  private void delete(String path, Handler<AsyncResult<Void>> handler) {
    Context context = vertx.getOrCreateContext();
    try {
      client.delete()
          .deletingChildrenIfNeeded()
          .inBackground((client, event) -> {
            context.runOnContext(v -> handler.handle(Future.succeededFuture()));
          })
          .forPath(path);
    } catch (Exception e) {
      handler.handle(Future.failedFuture(e));
    }
  }

  private void writeSomeConf(String path, boolean create, Handler<AsyncResult<Void>> handler) {
    Context context = vertx.getOrCreateContext();
    try {
      if (create) {
        JsonObject conf = new JsonObject().put("some-key", "some-value");
        client.create()
            .creatingParentsIfNeeded()
            .inBackground((client, event) -> dataWrittenCallback(handler, context, event))
            .forPath(path, conf.encode().getBytes());
      } else {
        JsonObject conf = new JsonObject().put("some-key", "some-value-2");
        client.setData()
            .inBackground((client, event) -> dataWrittenCallback(handler, context, event))
            .forPath(path, conf.encode().getBytes());
      }
    } catch (Exception e) {
      handler.handle(Future.failedFuture(e));
    }
  }

  private void dataWrittenCallback(Handler<AsyncResult<Void>> handler, Context context, CuratorEvent event) {
    context.runOnContext((x) -> {
          if (event.getResultCode() == 0) {
            handler.handle(Future.succeededFuture());
          } else {
            handler.handle(Future.failedFuture(KeeperException
                .create(KeeperException.Code.get(event.getResultCode()))));
          }
        }
    );
  }

}

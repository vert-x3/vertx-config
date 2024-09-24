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

package io.vertx.config.tests.verticle;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class VerticleRedeploymentTest {

  private Vertx vertx;
  private HttpServer server;
  private JsonObject http;
  private ArrayBlockingQueue<String> mark;

  @Before
  public void setUp(TestContext tc) throws Exception {
    mark = new ArrayBlockingQueue<>(20);
    vertx = Vertx.vertx();
    vertx.exceptionHandler(tc.exceptionHandler());

    vertx.eventBus().consumer("test.address", msg -> {
      mark.add(msg.body().toString());
    });

    http = new JsonObject().put("mark", "v1");

    server = vertx.createHttpServer()
        .requestHandler(request -> {
          if (request.path().endsWith("/conf")) {
            request.response().end(http.encodePrettily());
          }
        })
        .listen(8080).await(20, TimeUnit.SECONDS);

    vertx
      .deployVerticle(MyMainVerticle.class.getName())
      .await(20, TimeUnit.SECONDS);
  }

  @After
  public void tearDown() {
    server.close();
    vertx.close();
  }

  @Test
  public void testRedeployment() throws Exception {
    assertEquals("v1", mark.take());
    http.put("mark", "v2");
    assertEquals("v2", mark.take());
  }
}

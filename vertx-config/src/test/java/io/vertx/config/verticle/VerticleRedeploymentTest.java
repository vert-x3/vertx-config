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

package io.vertx.config.verticle;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class VerticleRedeploymentTest {

  private Vertx vertx;
  private HttpServer server;
  private JsonObject http;

  @Before
  public void setUp(TestContext tc) {
    vertx = Vertx.vertx();
    vertx.exceptionHandler(tc.exceptionHandler());

    http = new JsonObject().put("mark", "v1");

    AtomicBoolean done = new AtomicBoolean();
    vertx.createHttpServer()
        .requestHandler(request -> {
          if (request.path().endsWith("/conf")) {
            request.response().end(http.encodePrettily());
          }
        })
        .listen(8080).onComplete(tc.asyncAssertSuccess(s -> {
          done.set(true);
          server = s;
        }));

    await().untilAtomic(done, is(true));
    done.set(false);

    vertx.deployVerticle(MyMainVerticle.class.getName()).onComplete(deployed -> done.set(deployed.succeeded()));

    await().untilAtomic(done, is(true));
  }

  @After
  public void tearDown() {
    server.close();
    vertx.close();
  }

  @Test
  public void testRedeployment() {
    assertThat(MyVerticle.mark).isEqualTo("v1");
    http.put("mark", "v2");

    await().until(() -> MyVerticle.mark.equalsIgnoreCase("v2"));
  }

}

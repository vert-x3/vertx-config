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

package io.vertx.config.kubernetes;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.MalformedURLException;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class ConfigMapStoreOutsideOfKubernetesTest {

  protected Vertx vertx;
  private ConfigRetriever retriever;

  @Before
  public void setUp(TestContext tc) throws MalformedURLException {
    vertx = Vertx.vertx();
    vertx.exceptionHandler(tc.exceptionHandler());
    ConfigStoreOptions store = new ConfigStoreOptions()
      .setType("configmap")
      .setConfig(new JsonObject()
        .put("name", "my-config-map")
        .put("key", "my-app-json"));
    retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions().addStore(store));
  }

  @After
  public void tearDown() {
    vertx.close();
  }

  @Test
  public void test(TestContext tc) {
    Async async = tc.async();
    retriever.getConfig(ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
      }
      tc.assertTrue(ar.succeeded());
      tc.assertTrue(ar.result().isEmpty());
      async.complete();
    });
  }


}

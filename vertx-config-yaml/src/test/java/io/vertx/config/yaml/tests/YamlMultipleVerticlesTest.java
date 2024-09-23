/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.config.yaml.tests;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:aoingl@gmail.com">Lin Gao</a>
 */
@RunWith(VertxUnitRunner.class)
public class YamlMultipleVerticlesTest {
  private Vertx vertx;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
  }

  @After
  public void tearDown() {
    vertx.close();
  }

  private static class ConfigYamlVerticle extends AbstractVerticle {
    private ConfigRetriever retriever;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
      ConfigStoreOptions store = new ConfigStoreOptions()
        .setType("file")
        .setFormat("yaml")
        .setConfig(new JsonObject()
          .put("path", "src/test/resources/simple.yaml")
        );
      retriever = ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions().addStore(store));
      retriever.getConfig().onComplete(js -> {
        if (js.succeeded()) {
          String value = js.result().getString("key");
          Assert.assertEquals("value", value);
          startPromise.complete();
        } else {
          startPromise.fail(js.cause());
        }
      });
    }

    @Override
    public void stop() throws Exception {
      retriever.close();
    }
  }

  @Test
  public void testReadYamlConcurrent(TestContext testContext) {
    int instances = 4;
    vertx.deployVerticle(ConfigYamlVerticle::new, new DeploymentOptions().setInstances(instances))
      .onComplete(testContext.asyncAssertSuccess(va -> vertx.undeploy(va).onComplete(testContext.asyncAssertSuccess())));
  }

}

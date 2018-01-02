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

package io.vertx.config;

import io.vertx.config.impl.spi.ConfigChecker;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RepeatRule;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class ConfigurationStreamTest {

  private Vertx vertx;
  private ConfigRetriever retriever;
  private boolean doClose = true;

  @Rule
  public RepeatRule repeatRule = new RepeatRule();

  @Before
  public void setUp(TestContext tc) {
    vertx = Vertx.vertx();
    vertx.exceptionHandler(tc.exceptionHandler());

    System.setProperty("foo", "bar");
  }

  @After
  public void tearDown() {
    if (doClose) {
      retriever.close();
    }
    vertx.close();
    System.clearProperty("key");
    System.clearProperty("foo");
  }

  private static ConfigRetrieverOptions addStores(ConfigRetrieverOptions options) {
    return options
        .addStore(
            new ConfigStoreOptions()
                .setType("file")
                .setConfig(new JsonObject().put("path", "src/test/resources/file/regular.json")))
        .addStore(
            new ConfigStoreOptions()
                .setType("sys")
                .setConfig(new JsonObject().put("cache", false)));
  }

  @Test
  public void testRetrievingTheConfiguration(TestContext tc) {
    retriever = ConfigRetriever.create(vertx,
        addStores(new ConfigRetrieverOptions()));
    Async async = tc.async();
    retriever.configStream()
        .handler(conf -> {
          ConfigChecker.check(conf);
          assertThat(conf.getString("foo")).isEqualToIgnoringCase("bar");
          ConfigChecker.check(retriever.getCachedConfig());
          async.complete();
        });
  }

  @Test
  public void testRetrievingTheConfigurationAndClose(TestContext tc) {
    retriever = ConfigRetriever.create(vertx,
        addStores(new ConfigRetrieverOptions()));
    Async async = tc.async();
    doClose = false;
    retriever.configStream()
        .endHandler(v -> async.complete())
        .handler(conf -> {
          ConfigChecker.check(conf);
          assertThat(conf.getString("foo")).isEqualToIgnoringCase("bar");
          ConfigChecker.check(retriever.getCachedConfig());
          retriever.close();
        });
  }

  @Test
  public void testPauseResumeCycles(TestContext tc) {
    retriever = ConfigRetriever.create(vertx,
        addStores(new ConfigRetrieverOptions()));
    Async async = tc.async();
    AtomicInteger steps = new AtomicInteger();
    retriever.configStream()
        .handler(conf -> {
          if (steps.get() == 0) {
            assertThat(conf.getString("foo")).isEqualToIgnoringCase("bar");
            retriever.configStream().pause();
            System.setProperty("foo", "bar2");
            retriever.configStream().resume();
            steps.incrementAndGet();
          } else if (steps.get() == 1) {
            assertThat(conf.getString("foo")).isEqualToIgnoringCase("bar2");
            async.complete();
          }
        });
  }

}

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

package io.vertx.config.spring;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.config.server.ConfigServerApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * Check the behavior of the Spring config Server store. It starts a Spring Config Server application using the
 * default "sample" location.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class SpringConfigServerStoreTest {

  private ConfigRetriever retriever;
  private Vertx vertx;
  private static ConfigurableApplicationContext springConfigServer;


  @BeforeClass
  public static void initSpringConfigServer() {
    springConfigServer = new SpringApplicationBuilder(ConfigServerApplication.class)
        .properties("spring.config.name=configserver").run();
  }


  @AfterClass
  public static void shutdownSpringConfigServer() {
    springConfigServer.close();
  }

  @Before
  public void setUp(TestContext tc) {
    vertx = Vertx.vertx();
    vertx.exceptionHandler(tc.exceptionHandler());
  }


  @After
  public void tearDown() {
    retriever.close();
    vertx.close();
  }

  @Test
  public void testWithFooDev(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions().addStore(
            new ConfigStoreOptions()
                .setType("spring-config-server")
                .setConfig(new JsonObject().put("url", "http://localhost:8888/foo/development"))));


    retriever.getConfig(json -> {
      assertThat(json.succeeded()).isTrue();
      JsonObject config = json.result();

      assertThat(config.getString("bar")).isEqualToIgnoringCase("spam");
      assertThat(config.getString("foo")).isEqualToIgnoringCase("from foo development");
      assertThat(config.getString("info.description")).isEqualToIgnoringCase("Spring Cloud Samples");

      async.complete();
    });

  }

  @Test
  public void testWithStoresCloud(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions().addStore(
            new ConfigStoreOptions()
                .setType("spring-config-server")
                .setConfig(new JsonObject()
                    .put("url", "http://localhost:8888/stores/cloud")
                    .put("timeout", 10000))));


    retriever.getConfig(json -> {
      assertThat(json.succeeded()).isTrue();
      JsonObject config = json.result();

      assertThat(config.getInteger("hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds"))
          .isEqualTo(60000);
      assertThat(config.getString("eureka.password")).isEqualToIgnoringCase("password");
      assertThat(config.getString("spring.data.mongodb.uri")).isEqualToIgnoringCase("${vcap.services.${PREFIX:}mongodb.credentials.uri}");
      assertThat(config.getString("eureka.client.serviceUrl.defaultZone"))
        .isEqualToIgnoringCase("${vcap.services.${PREFIX:}eureka.credentials.uri:http://user:${eureka.password:}@${PREFIX:}eureka.${application.domain:cfapps.io}}/eureka/");

      async.complete();
    });

  }

  @Test
  public void testWithUnknownConfiguration(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions().addStore(
            new ConfigStoreOptions()
                .setType("spring-config-server")
                .setConfig(new JsonObject()
                    .put("url", "http://localhost:8888/missing/missing")
                    .put("timeout", 10000))));


    retriever.getConfig(json -> {
      assertThat(json.succeeded()).isTrue();
      JsonObject config = json.result();
      assertThat(config.getString("eureka.client.serviceUrl.defaultZone"))
          .isEqualToIgnoringCase("http://localhost:8761/eureka/");
      async.complete();
    });
  }

  @Test
  public void testWithErrorConfiguration(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions().addStore(
            new ConfigStoreOptions()
                .setType("spring-config-server")
                .setConfig(new JsonObject()
                    .put("url", "http://localhost:8888/missing/missing/missing")
                    .put("timeout", 10000))));


    retriever.getConfig(json -> {
      assertThat(json.succeeded()).isFalse();
      async.complete();
    });
  }

  @Test
  public void testWithWrongServerConfiguration(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions().addStore(
            new ConfigStoreOptions()
                .setType("spring-config-server")
                .setConfig(new JsonObject()
                    .put("url", "http://not-valid.de")
                    .put("timeout", 10000))));


    retriever.getConfig(json -> {
      assertThat(json.succeeded()).isFalse();
      async.complete();
    });
  }

}

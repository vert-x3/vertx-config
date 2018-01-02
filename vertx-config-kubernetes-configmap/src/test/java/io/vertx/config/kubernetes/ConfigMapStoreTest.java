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

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesMockServer;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class ConfigMapStoreTest {

  protected static final String SOME_JSON = new JsonObject().put("foo", "bar")
    .put("port", 8080)
    .put("debug", true).encode();
  protected static final String SOME_PROPS = "foo=bar\nport=8080\ndebug=true";

  protected Vertx vertx;
  protected KubernetesClient client;
  private ConfigMapStore store;
  protected KubernetesMockServer server;
  protected int port;

  @Before
  public void setUp(TestContext tc) throws MalformedURLException {
    vertx = Vertx.vertx();
    vertx.exceptionHandler(tc.exceptionHandler());

    ConfigMap map1 = new ConfigMapBuilder().withMetadata(new ObjectMetaBuilder().withName("my-config-map").build())
      .addToData("my-app-json", SOME_JSON)
      .addToData("my-app-props", SOME_PROPS)
      .build();

    Map<String, String> data = new LinkedHashMap<>();
    data.put("key", "value");
    data.put("bool", "true");
    data.put("count", "3");
    ConfigMap map2 = new ConfigMapBuilder().withMetadata(new ObjectMetaBuilder().withName("my-config-map-2").build())
      .withData(data)
      .build();

    ConfigMap map3 = new ConfigMapBuilder().withMetadata(new ObjectMetaBuilder().withName("my-config-map-x").build())
      .addToData("my-app-json", SOME_JSON)
      .build();

    Secret secret = new SecretBuilder().withMetadata(new ObjectMetaBuilder().withName("my-secret").build())
      .addToData("password", Base64.getEncoder().encodeToString("secret".getBytes(UTF_8)))
      .build();

    server = new KubernetesMockServer(false);


    server.expect().get().withPath("/api/v1/namespaces/default/configmaps").andReturn(200, new
      ConfigMapListBuilder().addToItems(map1, map2).build()).always();
    server.expect().get().withPath("/api/v1/namespaces/my-project/configmaps").andReturn(200, new
      ConfigMapListBuilder().addToItems(map3).build()).always();

    server.expect().get().withPath("/api/v1/namespaces/default/configmaps/my-config-map")
      .andReturn(200, map1).always();
    server.expect().get().withPath("/api/v1/namespaces/default/configmaps/my-config-map-2")
      .andReturn(200, map2).always();

    server.expect().get().withPath("/api/v1/namespaces/my-project/configmaps/my-config-map-x")
      .andReturn(200, map3).always();

    server.expect().get().withPath("/api/v1/namespaces/default/configmaps/my-unknown-config-map")
      .andReturn(500, null).always();
    server.expect().get().withPath("/api/v1/namespaces/default/configmaps/my-unknown-map")
      .andReturn(500, null).always();

    server.expect().get().withPath("/api/v1/namespaces/my-project/secrets/my-secret").andReturn(200, secret)
      .always();

    server.init();
    client = server.createClient();
    port = new URL(client.getConfiguration().getMasterUrl()).getPort();
  }

  private JsonObject config() {
    String token = client.getConfiguration().getOauthToken();
    if (token == null  || token.trim().isEmpty()) {
      token = "some-token";
    }
    return new JsonObject()
      .put("token", token)
      .put("host", "localhost")
      .put("ssl", false)
      .put("port", port);
  }

  @After
  public void tearDown() {
    store.close(null);
    vertx.close();
    server.shutdown();
  }

  @Test
  public void testWithJson(TestContext tc) {
    Async async = tc.async();
    store = new ConfigMapStore(vertx, config()
      .put("name", "my-config-map")
      .put("key", "my-app-json"));
    checkJsonConfig(tc, async);
  }

  @Test
  public void testWithUnknownConfigMap(TestContext tc) {
    Async async = tc.async();
    store = new ConfigMapStore(vertx, config()
      .put("name", "my-unknown-config-map")
      .put("optional", false));

    store.get(ar -> {
      assertThat(ar.failed()).isTrue();
      async.complete();
    });
  }

  @Test
  public void testWithUnknownSecrets(TestContext tc) {
    Async async = tc.async();
    store = new ConfigMapStore(vertx, config()
      .put("name", "my-unknown-secret")
      .put("optional", false)
      .put("secret", true));

    store.get(ar -> {
      assertThat(ar.failed()).isTrue();
      async.complete();
    });
  }

  @Test
  public void testWithSecret(TestContext tc) {
    Async async = tc.async();
    store = new ConfigMapStore(vertx, config()
      .put("name", "my-secret")
      .put("secret", true)
      .put("namespace", "my-project"));

    store.get(ar -> {
      tc.assertTrue(ar.succeeded());
      JsonObject json = ar.result().toJsonObject();
      assertThat(json.getString("password")).isEqualTo("secret");
      async.complete();
    });
  }

  private void checkJsonConfig(TestContext tc, Async async) {
    store.get(ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
      }
      tc.assertTrue(ar.succeeded());
      JsonObject json = ar.result().toJsonObject();
      tc.assertEquals(json.getString("foo"), "bar");
      tc.assertEquals(json.getInteger("port"), 8080);
      tc.assertTrue(json.getBoolean("debug"));
      async.complete();
    });
  }

  @Test
  public void testWithJsonInAnotherNamespace(TestContext tc) {
    Async async = tc.async();
    store = new ConfigMapStore(vertx, config()
      .put("name", "my-config-map-x")
      .put("namespace", "my-project")
      .put("key", "my-app-json"));

    checkJsonConfig(tc, async);
  }

  @Test
  public void testWithUnknownKey(TestContext tc) {
    Async async = tc.async();
    store = new ConfigMapStore(vertx, config()
      .put("name", "my-config-map").put("key", "my-unknown-key"));

    store.get(ar -> {
      tc.assertTrue(ar.failed());
      async.complete();
    });
  }

  @Test
  public void testWithUnknownMap(TestContext tc) {
    Async async = tc.async();
    store = new ConfigMapStore(vertx, config()
      .put("name", "my-unknown-map")
      .put("optional", false));

    store.get(ar -> {
      tc.assertTrue(ar.failed());
      async.complete();
    });
  }

  @Test
  public void testWithUnknownMapWithOptional(TestContext tc) {
    Async async = tc.async();
    store = new ConfigMapStore(vertx, config()
      .put("optional", true)
      .put("name", "my-unknown-map"));

    store.get(ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
      }
      tc.assertTrue(ar.succeeded());
      tc.assertEquals(ar.result().toString(), "{}");
      async.complete();
    });
  }

  @Test
  public void testWithProperties(TestContext tc) {
    Async async = tc.async();
    store = new ConfigMapStore(vertx, config()
      .put("name", "my-config-map").put("key", "my-app-props"));

    store.get(ar -> {
      tc.assertTrue(ar.succeeded());
      Properties properties = new Properties();
      try {
        properties.load(new StringReader(ar.result().toString()));
      } catch (IOException e) {
        tc.fail(e);
      }
      tc.assertEquals(properties.get("foo"), "bar");
      tc.assertEquals(properties.get("port"), "8080");
      tc.assertEquals(properties.get("debug"), "true");
      async.complete();
    });
  }

  @Test
  public void testWithoutKey(TestContext tc) {
    Async async = tc.async();
    store = new ConfigMapStore(vertx, config()
      .put("name", "my-config-map-2"));

    store.get(ar -> {
      if (! ar.succeeded()) {
        ar.cause().printStackTrace();
      }
      tc.assertTrue(ar.succeeded());

      JsonObject json = ar.result().toJsonObject();
      tc.assertEquals(json.getString("key"), "value");
      tc.assertEquals(json.getInteger("count"), 3);
      tc.assertTrue(json.getBoolean("bool"));
      async.complete();
    });
  }


}

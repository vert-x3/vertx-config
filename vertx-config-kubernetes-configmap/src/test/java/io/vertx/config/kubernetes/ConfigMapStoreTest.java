package io.vertx.config.kubernetes;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.mock.KubernetesMockClient;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class ConfigMapStoreTest {

  private static final String SOME_JSON = new JsonObject().put("foo", "bar")
      .put("port", 8080)
      .put("debug", true).encode();
  private static final String SOME_PROPS = "foo=bar\nport=8080\ndebug=true";

  private Vertx vertx;
  private KubernetesClient client;
  private ConfigMapStore store;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();

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
        .addToData("password", "secret")
        .build();

    KubernetesMockClient client = new KubernetesMockClient();
    client.configMaps().inNamespace("default").list().andReturn(new ConfigMapListBuilder().addToItems(map1, map2)
        .build());
    client.configMaps().inNamespace("default").withName("my-config-map").get().andReturn(map1);
    client.configMaps().inNamespace("default").withName("my-config-map-2").get().andReturn(map2);

    client.configMaps().inNamespace("my-project").list().andReturn(new ConfigMapListBuilder().addToItems(map3)
        .build());
    client.configMaps().inNamespace("my-project").withName("my-config-map-x").get().andReturn(map3);

    client.secrets().inNamespace("my-project").withName("my-secret").get().andReturn(secret);
    this.client = client.replay();
  }

  @After
  public void tearDown() {
    store.close(null);
    vertx.close();
  }

  @Test
  public void testWithJson(TestContext tc) {
    Async async = tc.async();
    store = new ConfigMapStore(vertx, new JsonObject()
        .put("name", "my-config-map").put("key", "my-app-json"));
    store.setClient(client);
    checkJsonConfig(tc, async);
  }

  @Test
  public void testWithUnknownConfigMap(TestContext tc) {
    Async async = tc.async();
    store = new ConfigMapStore(vertx, new JsonObject()
        .put("name", "my-unknown-config-map"));
    store.setClient(client);
    store.get(ar -> {
      assertThat(ar.failed()).isTrue();
      async.complete();
    });
  }

  @Test
  public void testWithUnknownSecrets(TestContext tc) {
    Async async = tc.async();
    store = new ConfigMapStore(vertx, new JsonObject()
        .put("name", "my-unknown-secret").put("secret", true));
    store.setClient(client);
    store.get(ar -> {
      assertThat(ar.failed()).isTrue();
      async.complete();
    });
  }

  @Test
  public void testWithSecret(TestContext tc) {
    Async async = tc.async();
    store = new ConfigMapStore(vertx, new JsonObject()
        .put("name", "my-secret")
        .put("secret", true)
        .put("namespace", "my-project"));
    store.setClient(client);
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
    store = new ConfigMapStore(vertx, new JsonObject()
        .put("name", "my-config-map-x")
        .put("namespace", "my-project")
        .put("key", "my-app-json"));
    store.setClient(client);
    checkJsonConfig(tc, async);
  }

  @Test
  public void testWithUnknownKey(TestContext tc) {
    Async async = tc.async();
    store = new ConfigMapStore(vertx, new JsonObject()
        .put("name", "my-config-map").put("key", "my-unknown-key"));
    store.setClient(client);
    store.get(ar -> {
      tc.assertTrue(ar.failed());
      async.complete();
    });
  }

  @Test
  public void testWithUnknownMap(TestContext tc) {
    Async async = tc.async();
    store = new ConfigMapStore(vertx, new JsonObject()
        .put("name", "my-unknown-map"));
    store.setClient(client);
    store.get(ar -> {
      tc.assertTrue(ar.failed());
      async.complete();
    });
  }

  @Test
  public void testWithProperties(TestContext tc) {
    Async async = tc.async();
    store = new ConfigMapStore(vertx, new JsonObject()
        .put("name", "my-config-map").put("key", "my-app-props"));
    store.setClient(client);
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
    store = new ConfigMapStore(vertx, new JsonObject()
        .put("name", "my-config-map-2"));
    store.setClient(client);
    store.get(ar -> {
      tc.assertTrue(ar.succeeded());

      JsonObject json = ar.result().toJsonObject();
      tc.assertEquals(json.getString("key"), "value");
      tc.assertEquals(json.getInteger("count"), 3);
      tc.assertTrue(json.getBoolean("bool"));
      async.complete();
    });
  }


}
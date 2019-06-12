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

package io.vertx.config.vault;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.config.impl.ConfigRetrieverImpl;
import io.vertx.config.impl.ConfigurationProvider;
import io.vertx.config.vault.client.SlimVaultClient;
import io.vertx.config.vault.utils.VaultProcess;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.Is.is;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public abstract class VaultConfigStoreTestBase {

  protected static VaultProcess process;
  protected Vertx vertx;
  SlimVaultClient client;
  ConfigRetriever retriever;


  @BeforeClass
  public static void setupClass() throws IOException, InterruptedException {
    process = new VaultProcess();
    process.initAndUnsealVault();
    assert process.isRunning();
  }

  @AfterClass
  public static void tearDownClass() {
    process.shutdown();
  }

  @Before
  public void setup(TestContext tc) {
    Async async = tc.async();
    vertx = Vertx.vertx();
    client = new SlimVaultClient(vertx, process.getConfigurationWithRootToken());

    JsonObject secret = new JsonObject().put("message", "hello").put("counter", 10)
      .put("nested", new JsonObject().put("foo", "bar"))
      .put("props", "key=val\nkey2=5\n");

    Promise<Void> keyValueV1Promise = Promise.promise();

    client.write("secret/app/foo", secret, ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
      }
      tc.assertTrue(ar.succeeded());
      client.write("secret/app/update", secret, ar2 -> {
        tc.assertTrue(ar.succeeded());
        keyValueV1Promise.complete();
      });
    });

    Promise<Void> keyValueV2Promise = Promise.promise();

    client.write("secret-v2/data/app/foo", new JsonObject().put("data", secret), ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
      }
      tc.assertTrue(ar.succeeded());
      client.write("secret-v2/data/app/update", new JsonObject().put("data", secret), ar2 -> {
        tc.assertTrue(ar.succeeded());
        keyValueV2Promise.complete();
      });
    });

    CompositeFuture.all(keyValueV1Promise.future(), keyValueV2Promise.future()).setHandler(h -> {
      vertx.executeBlocking(future -> {
        configureVault();
        future.complete();
      }, x -> async.complete());
    });
  }

  /**
   * Implemented by concrete tests to configure Vault (especially backends).
   */
  protected void configureVault() {
    // Nothing by default
  }

  @After
  public void tearDown(TestContext tc) {
    if (retriever != null) {
      retriever.close();
    }
    vertx.close(tc.asyncAssertSuccess());
  }

  /**
   * Tests the access to a secret with KV-v1 engine.
   */
  @Test
  public void testAccessToSecretV1(TestContext tc) {
    JsonObject additionalConfig = getRetrieverConfiguration();
    JsonObject config = additionalConfig.copy().put("path", "secret/app/foo");

    retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions()
      .addStore(new ConfigStoreOptions().setType("vault").setConfig(config)));

    Async async = tc.async();

    retriever.getConfig(json -> {
      tc.assertTrue(json.succeeded());
      JsonObject content = json.result();
      tc.assertEquals("hello", content.getString("message"));
      tc.assertEquals(10, content.getInteger("counter"));

      async.complete();
    });
  }

  /**
   * Tests the access to a secret with KV-v2 engine.
   */
  @Test
  public void testAccessToSecretV2(TestContext tc) {
    JsonObject additionalConfig = getRetrieverConfiguration();
    JsonObject config = additionalConfig.copy().put("path", "secret-v2/data/app/foo");

    retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions()
      .addStore(new ConfigStoreOptions().setType("vault").setConfig(config)));

    Async async = tc.async();

    retriever.getConfig(json -> {
      tc.assertTrue(json.succeeded());
      JsonObject content = json.result();
      tc.assertEquals("hello", content.getString("message"));
      tc.assertEquals(10, content.getInteger("counter"));

      async.complete();
    });
  }

  /**
   * Tests the access to a specific key of a secret in KV-v1 engine.
   */
  @Test
  public void testAccessToNestedContentFromSecretV1(TestContext tc) {
    JsonObject additionalConfig = getRetrieverConfiguration();
    JsonObject config = additionalConfig.copy()
      .put("path", "secret/app/foo").put("key", "nested");

    retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions()
      .addStore(new ConfigStoreOptions().setType("vault")
        .setConfig(config)));

    Async async = tc.async();

    retriever.getConfig(json -> {
      if (json.failed()) {
        json.cause().printStackTrace();
      }
      tc.assertTrue(json.succeeded());
      JsonObject content = json.result();
      tc.assertEquals(content.getString("foo"), "bar");
      async.complete();
    });
  }

  /**
   * Tests the access to a specific key of a secret in KV-v2 engine.
   */
  @Test
  public void testAccessToNestedContentFromSecretV2(TestContext tc) {
    JsonObject additionalConfig = getRetrieverConfiguration();
    JsonObject config = additionalConfig.copy()
      .put("path", "secret-v2/data/app/foo").put("key", "nested");

    retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions()
      .addStore(new ConfigStoreOptions().setType("vault")
        .setConfig(config)));

    Async async = tc.async();

    retriever.getConfig(json -> {
      if (json.failed()) {
        json.cause().printStackTrace();
      }
      tc.assertTrue(json.succeeded());
      JsonObject content = json.result();
      tc.assertEquals(content.getString("foo"), "bar");
      async.complete();
    });
  }

  /**
   * Tests the access to secret data encoded as properties with KV-v1 engine.
   */
  @Test
  public void testAccessToNestedContentAsPropertiesFromSecretV1(TestContext tc) {
    JsonObject additionalConfig = getRetrieverConfiguration();
    JsonObject config = additionalConfig.copy().put("path", "secret/app/foo").put("key", "props");

    retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions()
      .addStore(new ConfigStoreOptions().setType("vault")
        .setFormat("properties")
        .setConfig(config)));

    Async async = tc.async();

    retriever.getConfig(json -> {
      tc.assertTrue(json.succeeded());
      JsonObject content = json.result();
      tc.assertEquals(content.getString("key"), "val");
      tc.assertEquals(content.getInteger("key2"), 5);
      async.complete();
    });
  }

  /**
   * Tests the access to secret data encoded as properties with KV-v2 engine.
   */
  @Test
  public void testAccessToNestedContentAsPropertiesFromSecretV2(TestContext tc) {
    JsonObject additionalConfig = getRetrieverConfiguration();
    JsonObject config = additionalConfig.copy().put("path", "secret-v2/data/app/foo").put("key", "props");

    retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions()
      .addStore(new ConfigStoreOptions().setType("vault")
        .setFormat("properties")
        .setConfig(config)));

    Async async = tc.async();

    retriever.getConfig(json -> {
      tc.assertTrue(json.succeeded());
      JsonObject content = json.result();
      tc.assertEquals(content.getString("key"), "val");
      tc.assertEquals(content.getInteger("key2"), 5);
      async.complete();
    });
  }

  /**
   * Tests the periodic configuration updates.
   */
  @Test
  public void testConfigurationUpdates(TestContext tc) {
    JsonObject additionalConfig = getRetrieverConfiguration();
    JsonObject config = additionalConfig.copy()
      .put("path", "secret/app/update");

    retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions()
      .addStore(new ConfigStoreOptions().setType("vault")
        .setConfig(config)).setScanPeriod(1000));

    Async async = tc.async();

    retriever.configStream().handler(json -> {
      if (json.getString("message").equals("bonjour")
        && json.getInteger("counter") == 11
        && json.getBoolean("enabled")) {
        async.complete();
      }
    });

    AtomicBoolean done = new AtomicBoolean();
    retriever.getConfig(json -> {
      tc.assertTrue(json.succeeded());
      JsonObject content = json.result();
      tc.assertEquals("hello", content.getString("message"));
      done.set(true);

    });

    await().untilAtomic(done, is(true));
    process.run("write " + VaultProcess.CA_CERT_ARG + " secret/app/update @src/test/resources/some-secret.json");
  }

  /**
   * Tests accessing a missing secret.
   */
  @Test
  public void testRetrievingAMissingSecret(TestContext tc) {
    JsonObject additionalConfig = getRetrieverConfiguration();
    JsonObject config = additionalConfig.copy()
      .put("path", "secret/app/missing");

    retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions()
      .addStore(new ConfigStoreOptions().setType("vault")
        .setConfig(config)));

    Async async = tc.async();

    retriever.getConfig(json -> {
      tc.assertTrue(json.succeeded());
      JsonObject content = json.result();
      tc.assertTrue(content.isEmpty());
      async.complete();
    });
  }

  /**
   * Tests accessing a specific key from a missing secret.
   */
  @Test
  public void testRetrievingAKeyFromAMissingSecret(TestContext tc) {
    JsonObject additionalConfig = getRetrieverConfiguration();
    JsonObject config = additionalConfig.copy()
      .put("path", "secret/app/missing").put("key", "missing");

    retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions()
      .addStore(new ConfigStoreOptions().setType("vault")
        .setConfig(config)));

    Async async = tc.async();

    retriever.getConfig(json -> {
      tc.assertTrue(json.succeeded());
      tc.assertTrue(json.result().isEmpty());
      async.complete();
    });
  }

  /**
   * Tests accessing a missing key from an existing secret.
   */
  @Test
  public void testRetrievingAMissingKey(TestContext tc) {
    JsonObject additionalConfig = getRetrieverConfiguration();
    JsonObject config = additionalConfig.copy()
      .put("path", "secret/app/foo").put("key", "missing");

    retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions()
      .addStore(new ConfigStoreOptions().setType("vault")
        .setConfig(config)));

    Async async = tc.async();

    retriever.getConfig(json -> {
      tc.assertTrue(json.succeeded());
      tc.assertTrue(json.result().isEmpty());
      async.complete();
    });
  }

  /**
   * Tests accessing a secret using a revoked root token. This test restarts Vault.
   */
  @Test
  public void testWithRevokedRootToken(TestContext tc) {
    Async async = tc.async();

    JsonObject additionalConfig = getRetrieverConfiguration();
    JsonObject config = additionalConfig.copy().put("path", "secret/app/foo");

    retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions()
      .addStore(new ConfigStoreOptions().setType("vault").setConfig(config)));

    // Step 1 - we are able to read the config
    retriever.getConfig(json -> {
      tc.assertTrue(json.succeeded());
      JsonObject content = json.result();
      tc.assertEquals("hello", content.getString("message"));
      // Step 2 - revoke the (here it's the root token) token
      vertx.executeBlocking(future -> {
        process.run("token revoke " + VaultProcess.CA_CERT_ARG + " -self");
        // Generate another token - restart vault.
        process.shutdown();
        process.initAndUnsealVault();
        future.complete();
      }, step2 -> {
        // Step 3 - attempt to retrieve the config
        retriever.getConfig(json2 -> {
          tc.assertTrue(json2.failed());
          tc.assertTrue(json2.cause().getMessage().contains("permission denied"));
          async.complete();
        });
      });
    });
  }

  /**
   * Tests accessing a secret when the Vault is sealed.
   */
  @Test
  public void testWithSealedVault(TestContext tc) {
    Async async = tc.async();

    JsonObject additionalConfig = getRetrieverConfiguration();
    JsonObject config = additionalConfig.copy().put("path", "secret/app/foo");

    retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions()
      .addStore(new ConfigStoreOptions().setType("vault").setConfig(config)));

    // Step 1 - we are able to read the config
    retriever.getConfig(json -> {
      tc.assertTrue(json.succeeded());
      JsonObject content = json.result();
      tc.assertEquals("hello", content.getString("message"));
      // Step 2 - revoke the (here it's the root token) token
      vertx.executeBlocking(future -> {
        process.run("operator seal " + VaultProcess.CA_CERT_ARG);
        future.complete();
      }, step2 -> {
        // Step 3 - attempt to retrieve the config
        retriever.getConfig(json2 -> {
          tc.assertTrue(json2.failed());
          tc.assertTrue(json2.cause().getMessage().contains("Vault is sealed"));

          // Step 4 - Unseal
          vertx.executeBlocking(fut -> {
            process.unseal();
            fut.complete();
          }, x -> {
            tc.assertTrue(x.succeeded());
            async.complete();
          });
        });
      });
    });
  }

  /**
   * Tests accessing a secret using a revoked token.
   */
  @Test
  public void testWithRevokedToken(TestContext tc) {
    Async async = tc.async();

    JsonObject additionalConfig = getRetrieverConfiguration();
    JsonObject config = additionalConfig.copy().put("path", "secret/app/foo");

    retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions()
      .addStore(new ConfigStoreOptions().setType("vault").setConfig(config)));

    // Step 1 - we are able to read the config
    retriever.getConfig(json -> {
      tc.assertTrue(json.succeeded());
      JsonObject content = json.result();
      tc.assertEquals("hello", content.getString("message"));
      // Step 2 - revoke the (here it's the root token) token
      vertx.executeBlocking(future -> {
        process.run("token revoke " + VaultProcess.CA_CERT_ARG + " " + extractCurrentToken());
        future.complete();
      }, step2 -> {
        // Step 3 - attempt to retrieve the config
        retriever.getConfig(json2 -> {
          tc.assertTrue(json2.failed());
          tc.assertTrue(json2.cause().getMessage().contains("permission denied"));
          async.complete();
        });
      });
    });
  }


  private String extractCurrentToken() {
    List<ConfigurationProvider> providers = ((ConfigRetrieverImpl) retriever).getProviders();
    for (ConfigurationProvider provider : providers) {
      if (provider.getStore() instanceof VaultConfigStore) {
        return ((VaultConfigStore) provider.getStore()).getVaultClient().getToken();
      }
    }
    return null;
  }

  /**
   * @return the additional configuration for the Vault config store.
   */
  protected abstract JsonObject getRetrieverConfiguration();

}

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

package io.vertx.config.vault.client;

import io.vertx.config.vault.utils.VaultProcess;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.UUID;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class VaultClientWithCertTest {


  private static VaultProcess process;
  private Vertx vertx;
  private SlimVaultClient client;

  @BeforeClass
  public static void setupClass() throws IOException, InterruptedException {
    process = new VaultProcess();
    process.initAndUnsealVault();
    process.setupBackendCert();
    assert process.isRunning();
  }

  @AfterClass
  public static void tearDownClass() {
    process.shutdown();
  }

  public static Handler<AsyncResult<Auth>> getLoginTestHandler(SlimVaultClient client, TestContext tc) {
    Async async = tc.async();
    String rw_path = "secret/app/hello-" + UUID.randomUUID().toString();
    String value = "world " + UUID.randomUUID().toString();
    return ar -> {
      tc.assertTrue(ar.succeeded());
      String token = ar.result().getClientToken();
      tc.assertNotNull(token);

      client.setToken(token);

      // Try to write and read some secrets - using the "user" policy
      client.write(rw_path, new JsonObject().put("value", value), x -> {
        if (x.failed()) {
          x.cause().printStackTrace();
        }
        tc.assertTrue(x.succeeded());
        client.read(rw_path, y -> {
          tc.assertTrue(y.succeeded());
          tc.assertEquals(value, y.result().getData().getString("value"));
          async.complete();
        });
      });
    };
  }

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    client = new SlimVaultClient(vertx, process.getConfigurationWithRootToken());
  }

  @After
  public void tearDown(TestContext tc) {
    vertx.close().onComplete(tc.asyncAssertSuccess());
  }

  /**
   * Tests authentication with the cert auth backend using PEM file
   */
  @Test
  public void testLoginByCert_usingPemConfig(TestContext tc) throws VaultException {
    JsonObject config = new JsonObject();
    config.put("host", process.getHost());
    config.put("port", process.getPort());
    config.put("ssl", true);
    PemKeyCertOptions options = new PemKeyCertOptions()
      .addCertPath("target/vault/config/ssl/client-cert.pem")
      .addKeyPath("target/vault/config/ssl/client-privatekey.pem");
    config.put("pemKeyCertOptions", options.toJson());

    PemTrustOptions trust = new PemTrustOptions()
      .addCertPath("target/vault/config/ssl/cert.pem");
    config.put("pemTrustStoreOptions", trust.toJson());

    JksOptions jks = new JksOptions()
      .setPath("target/vault/config/ssl/truststore.jks");
    config.put("trustStoreOptions", jks.toJson());

    client = new SlimVaultClient(vertx, config);

    checkWeCanLoginAndAccessRestrictedSecrets(tc);
  }

  /**
   * Tests authentication with the cert auth backend using PEM file
   */
  @Test
  public void testLoginByCert_usingJKSConfig(TestContext tc) throws VaultException {
    JsonObject config = new JsonObject();
    config.put("host", process.getHost());
    config.put("port", process.getPort());
    config.put("ssl", true);
    JksOptions options = new JksOptions();
    options.setPassword("password").setPath("target/vault/config/ssl/keystore.jks");
    config.put("keyStoreOptions", options.toJson());

    JksOptions jks = new JksOptions()
      .setPassword("password")
      .setPath("target/vault/config/ssl/truststore.jks");
    config.put("trustStoreOptions", jks.toJson());

    client = new SlimVaultClient(vertx, config);

    checkWeCanLoginAndAccessRestrictedSecrets(tc);
  }

  private void checkWeCanLoginAndAccessRestrictedSecrets(TestContext tc) {
    client.loginWithCert(getLoginTestHandler(client, tc));
  }


}

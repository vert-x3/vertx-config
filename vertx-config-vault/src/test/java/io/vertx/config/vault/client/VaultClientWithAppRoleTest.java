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
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.awaitility.Awaitility.await;


/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class VaultClientWithAppRoleTest {


  private static VaultProcess process;
  private Vertx vertx;
  private SlimVaultClient client;
  private String appRoleId;
  private String secretId;

  @BeforeClass
  public static void setupClass() throws IOException, InterruptedException {
    process = new VaultProcess();
    process.initAndUnsealVault();
    process.setupBackendAppRole();
    assert process.isRunning();
  }

  @AfterClass
  public static void tearDownClass() {
    process.shutdown();
  }

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    client = new SlimVaultClient(vertx, process.getConfigurationWithRootToken());

    client.read("auth/approle/role/testrole/role-id",
      secret -> appRoleId = secret.result().getData().getString("role_id"));

    client.write("auth/approle/role/testrole/secret-id", new JsonObject(),
      secret -> secretId = secret.result().getData().getString("secret_id"));

    await().until(() -> appRoleId != null && secretId != null);
  }

  @After
  public void tearDown(TestContext tc) {
    vertx.close(tc.asyncAssertSuccess());
  }

  /**
   * Tests authentication with the app role auth backend
   */
  @Test
  public void testLoginByAppRole(TestContext tc) throws VaultException {
    client = new SlimVaultClient(vertx, process.getConfiguration());
    client.loginWithAppRole(appRoleId, secretId,
      VaultClientWithCertTest.getLoginTestHandler(client, tc));
  }


}

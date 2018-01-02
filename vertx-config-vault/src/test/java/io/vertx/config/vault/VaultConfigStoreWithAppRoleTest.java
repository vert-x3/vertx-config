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

import io.vertx.config.vault.client.SlimVaultClient;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.runner.RunWith;

import static org.awaitility.Awaitility.await;

/**
 * Tests the behavior when using the approle backend.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class VaultConfigStoreWithAppRoleTest extends VaultConfigStoreTestBase {

  private String appRoleId;
  private String secretId;

  @Override
  protected void configureVault() {
    process.setupBackendAppRole();

    client = new SlimVaultClient(vertx, process.getConfigurationWithRootToken());

    client.read("auth/approle/role/testrole/role-id",
      secret -> appRoleId = secret.result().getData().getString("role_id"));

    client.write("auth/approle/role/testrole/secret-id", new JsonObject(),
      secret -> secretId = secret.result().getData().getString("secret_id"));

    await().until(() -> appRoleId != null && secretId != null);

    assert process.isRunning();
  }

  @Override
  protected JsonObject getRetrieverConfiguration() {
    return process.getConfiguration().copy()
      .put("auth-backend", "approle")
      .put("approle", new JsonObject()
        .put("role-id", appRoleId).put("secret-id", secretId)
      );
  }


}

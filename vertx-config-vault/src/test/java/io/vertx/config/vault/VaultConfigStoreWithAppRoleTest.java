package io.vertx.config.vault;

import io.vertx.config.vault.client.SlimVaultClient;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.runner.RunWith;

import static com.jayway.awaitility.Awaitility.await;

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

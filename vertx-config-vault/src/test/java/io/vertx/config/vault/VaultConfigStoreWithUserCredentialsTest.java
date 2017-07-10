package io.vertx.config.vault;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.runner.RunWith;

/**
 * Tests the behavior when using the userpass backend.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class VaultConfigStoreWithUserCredentialsTest extends VaultConfigStoreTestBase {

  @Override
  protected void configureVault() {
    process.setupBackendUserPass();
    assert process.isRunning();
  }

  @Override
  protected JsonObject getRetrieverConfiguration() {
    return process.getConfiguration().copy()
      .put("auth-backend", "userpass")
      .put("user-credentials", new JsonObject()
        .put("username", "fake-user").put("password", "fake-password")
      );
  }

}

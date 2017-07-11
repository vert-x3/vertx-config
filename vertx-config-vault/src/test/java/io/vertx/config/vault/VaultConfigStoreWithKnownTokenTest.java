package io.vertx.config.vault;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.runner.RunWith;

/**
 * Tests the behavior when using a known token (root token).
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class VaultConfigStoreWithKnownTokenTest extends VaultConfigStoreTestBase {


  @Override
  protected JsonObject getRetrieverConfiguration() {
    return process.getConfigurationWithRootToken();
  }


  @Override
  public void testWithRevokedToken(TestContext tc) {
    // We are using the root token - so this use case is already tested in another test.
  }
}

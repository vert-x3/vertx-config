package io.vertx.config.vault.client;

import io.vertx.config.vault.utils.VaultProcess;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.io.IOException;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class VaultClientWithUsernameTest {


  private static VaultProcess process;
  private Vertx vertx;
  private SlimVaultClient client;

  @BeforeClass
  public static void setupClass() throws IOException, InterruptedException {
    process = new VaultProcess();
    process.initAndUnsealVault();
    process.setupBackendUserPass();
    assert process.isRunning();
  }

  @AfterClass
  public static void tearDownClass() {
    process.shutdown();
  }

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    client = new SlimVaultClient(vertx, process.getConfiguration());
  }

  @After
  public void tearDown(TestContext tc) {
    vertx.close(tc.asyncAssertSuccess());
  }

  /**
   * Tests authentication with the username / password auth backend.
   */
  @Test
  public void testLoginWithUsername(TestContext tc) throws VaultException {
    client = new SlimVaultClient(vertx, process.getConfiguration());
    client.loginWithUserCredentials("fake-user", "fake-password",
      VaultClientWithCertTest.getLoginTestHandler(client, tc));
  }


}

package io.vertx.config.vault;

import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.runner.RunWith;

/**
 * Tests the behavior when using the cert backend (TLS certificates).
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class VaultConfigStoreWithCertsTest extends VaultConfigStoreTestBase {

  @Override
  protected void configureVault() {
    process.setupBackendCert();
    assert process.isRunning();
  }

  @Override
  protected JsonObject getRetrieverConfiguration() {

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

    config.put("auth-backend", "cert");

    return config;
  }

  // TODO redo revoked token - with the right token


}

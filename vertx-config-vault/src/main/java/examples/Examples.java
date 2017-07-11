package examples;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PemTrustOptions;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Examples {


  public void example1(Vertx vertx, JsonObject config) {
    ConfigStoreOptions store = new ConfigStoreOptions()
      .setType("vault")
      .setConfig(config);

    ConfigRetriever retriever = ConfigRetriever.create(vertx,
      new ConfigRetrieverOptions().addStore(store));
  }

  public void example1WithConfig(Vertx vertx) {
    JsonObject vault_config = new JsonObject()
      .put("host", "127.0.0.1") // The host name
      .put("port", 8200) // The port
      .put("ssl", true); // Whether or not SSL is used (disabled by default)

    // Certificates
    PemKeyCertOptions certs = new PemKeyCertOptions()
      .addCertPath("target/vault/config/ssl/client-cert.pem")
      .addKeyPath("target/vault/config/ssl/client-privatekey.pem");
    vault_config.put("pemKeyCertOptions", certs.toJson());

    // Truststore
    JksOptions jks = new JksOptions()
      .setPath("target/vault/config/ssl/truststore.jks");
    vault_config.put("trustStoreOptions", jks.toJson());

    // Path to the secret to read.
    vault_config.put("path", "secret/my-secret");

    ConfigStoreOptions store = new ConfigStoreOptions()
      .setType("vault")
      .setConfig(vault_config);

    ConfigRetriever retriever = ConfigRetriever.create(vertx,
      new ConfigRetrieverOptions().addStore(store));
  }

  public void exampleWithToken(Vertx vertx, String token) {
    JsonObject vault_config = new JsonObject();

    // ...

    // Path to the secret to read.
    vault_config.put("path", "secret/my-secret");

    // The token
    vault_config.put("token", token);

    ConfigStoreOptions store = new ConfigStoreOptions()
      .setType("vault")
      .setConfig(vault_config);

    ConfigRetriever retriever = ConfigRetriever.create(vertx,
      new ConfigRetrieverOptions().addStore(store));
  }

  public void exampleWithTokenCreation(Vertx vertx, String token) {
    JsonObject vault_config = new JsonObject();

    // ...

    // Path to the secret to read.
    vault_config.put("path", "secret/my-secret");

    // Configure the token generation

    // Configure the token request (https://www.vaultproject.io/docs/auth/token.html)
    JsonObject tokenRequest = new JsonObject()
      .put("ttl", "1h")
      .put("noDefault", true)

      // The token to use to request the generation (parts of the tokenRequest object)
      .put("token", token);

    vault_config.put("auth-backend", "token") // Indicate the auth backend to use
      .put("renew-window", 5000L) // Renew error margin in ms
      .put("token-request", tokenRequest); // Pass the token generation configuration

    ConfigStoreOptions store = new ConfigStoreOptions()
      .setType("vault")
      .setConfig(vault_config);

    ConfigRetriever retriever = ConfigRetriever.create(vertx,
      new ConfigRetrieverOptions().addStore(store));
  }


  public void exampleWithCerts(Vertx vertx) {
    JsonObject vault_config = new JsonObject();

    // ...

    PemKeyCertOptions certs = new PemKeyCertOptions()
      .addCertPath("target/vault/config/ssl/client-cert.pem")
      .addKeyPath("target/vault/config/ssl/client-privatekey.pem");
    vault_config.put("pemKeyCertOptions", certs.toJson());

    PemTrustOptions trust = new PemTrustOptions()
      .addCertPath("target/vault/config/ssl/cert.pem");
    vault_config.put("pemTrustStoreOptions", trust.toJson());

    JksOptions jks = new JksOptions()
      .setPath("target/vault/config/ssl/truststore.jks");
    vault_config.put("trustStoreOptions", jks.toJson());

    vault_config.put("auth-backend", "cert");

    // Path to the secret to read.
    vault_config.put("path", "secret/my-secret");

    ConfigStoreOptions store = new ConfigStoreOptions()
      .setType("vault")
      .setConfig(vault_config);

    ConfigRetriever retriever = ConfigRetriever.create(vertx,
      new ConfigRetrieverOptions().addStore(store));
  }

  public void exampleWithAppRole(Vertx vertx, String appRoleId, String secretId) {
    JsonObject vault_config = new JsonObject();

    // ...

    vault_config
      .put("auth-backend", "approle") // Set the auth-backend to approle
      .put("approle", new JsonObject()  // Configure the role id and secret it
        .put("role-id", appRoleId).put("secret-id", secretId)
      );

    // Path to the secret to read.
    vault_config.put("path", "secret/my-secret");

    ConfigStoreOptions store = new ConfigStoreOptions()
      .setType("vault")
      .setConfig(vault_config);

    ConfigRetriever retriever = ConfigRetriever.create(vertx,
      new ConfigRetrieverOptions().addStore(store));
  }

  public void exampleWithUserPass(Vertx vertx, String username, String password) {
    JsonObject vault_config = new JsonObject();

    // ...

    vault_config
      .put("auth-backend", "userpass") // Set the auth-backend to userpass
      .put("user-credentials", new JsonObject()
        .put("username", username).put("password", password)
      );

    // Path to the secret to read.
    vault_config.put("path", "secret/my-secret");

    ConfigStoreOptions store = new ConfigStoreOptions()
      .setType("vault")
      .setConfig(vault_config);

    ConfigRetriever retriever = ConfigRetriever.create(vertx,
      new ConfigRetrieverOptions().addStore(store));
  }

}

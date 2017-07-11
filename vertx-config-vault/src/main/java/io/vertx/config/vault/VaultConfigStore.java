package io.vertx.config.vault;

import io.vertx.config.spi.ConfigStore;
import io.vertx.config.vault.client.*;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import java.util.Objects;

/**
 * An implementation of {@link ConfigStore} for Vault (https://www.vaultproject.io/).
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class VaultConfigStore implements ConfigStore {

  private final SlimVaultClient client;
  private final JsonObject config;
  private final String path;
  private final Vertx vertx;

  /**
   * When authenticated, whether or not the token is renewable.
   */
  private boolean renewable;

  /**
   * The time when the token must be renewed.
   */
  private long validity;

  private Context context;

  /**
   * Creates an instance of {@link VaultConfigStore}.
   *
   * @param vertx  the vert.x instance
   * @param config the configuration, used to create the {@link SlimVaultClient}.
   */
  public VaultConfigStore(Vertx vertx, JsonObject config) {
    client = new SlimVaultClient(vertx, config);
    this.config = config;
    this.vertx = vertx;
    this.path = Objects.requireNonNull(config.getString("path"), "The path of the secret must be set");
  }

  @Override
  public void close(Handler<Void> completionHandler) {
    client.close();
    completionHandler.handle(null);
  }

  @Override
  public void get(Handler<AsyncResult<Buffer>> completionHandler) {
    if (context == null) {
      context = vertx.getOrCreateContext();
    }

    Handler<Void> actions = x -> {
      authenticate(false) // Are we logged in
        .compose(v -> renew()) // Do we need to renew
        .compose(v -> retrieve()) // Retrieve the data
        .compose(this::extract) // Extract the sub set
        .setHandler(completionHandler); // Done
    };

    if (Vertx.currentContext() == context) {
      actions.handle(null);
    } else {
      context.runOnContext(actions);
    }
  }

  private Future<Buffer> extract(JsonObject json) {
    Future<Buffer> future = Future.future();
    if (json == null) {
      future.complete(new JsonObject().toBuffer());
    } else if (config.getString("key") != null) {
      Object value = json.getValue(config.getString("key"));
      if (value == null) {
        future.complete(new JsonObject().toBuffer());
      } else if (value instanceof String) {
        future.complete(Buffer.buffer((String) value));
      } else if (value instanceof JsonObject) {
        future.complete(((JsonObject) value).toBuffer());
      }
    } else {
      future.complete(json.toBuffer());
    }
    return future;
  }

  private Future<JsonObject> retrieve() {
    Future<JsonObject> future = Future.future();
    client.read(path, ar -> {
      if (ar.failed() && !(ar.cause() instanceof VaultException)) {
        future.fail(ar.cause());
      } else if (ar.failed() && ((VaultException) ar.cause()).getStatusCode() == 404) {
        future.complete(null);
      } else if (ar.failed()) {
        future.fail(ar.cause());
      } else {
        Secret result = ar.result();
        JsonObject copy = result.getData().copy();
        copy.put("vault-lease-id", result.getLeaseId());
        copy.put("vault-lease-duration", result.getLeaseDuration());
        copy.put("vault-renewable", result.isRenewable());
        future.complete(copy);
      }
    });
    return future;
  }

  private Future<Void> renew() {
    Future<Void> future = Future.future();
    if (validity == 0) {
      // Using a root token - should not expire
      future.complete();
    } else {
      if (shouldBeRenewed() && renewable) {
        return renewToken();
      } else if (shouldBeRenewed()) {
        // The token cannot be renewed, attempt to authenticate again
        return authenticate(true);
      } else {
        future.complete();
      }
    }
    return future;
  }

  private Future<Void> renewToken() {
    Future<Void> future = Future.future();
    client.renewSelf(config.getLong("lease-duration", 3600L), auth -> {
      manageAuthenticationResult(future, auth);
    });
    return future;
  }


  private Future<Void> authenticate(boolean renew) {
    Future<Void> future = Future.future();
    if (!renew && client.getToken() != null) {
      future.complete();
      return future;
    }

    String policy = config.getString("auth-backend");
    Objects.requireNonNull(policy, "If you don't provide a token, the auth-backend must be set");
    switch (policy.toLowerCase()) {
      case "token":
        return loginWithToken();
      case "approle":
        return loginWithAppRole();
      case "cert":
        return loginWithCert();
      case "userpass":
        return loginWithUserName();
      default:
        throw new IllegalArgumentException("Non supported auth-backend: " + policy);
    }
  }

  private Future<Void> loginWithUserName() {
    Future<Void> future = Future.future();
    JsonObject req = config.getJsonObject("user-credentials");
    Objects.requireNonNull(req, "When using username, the `user-credentials` must be set in the " +
      "configuration");
    String username = req.getString("username");
    String password = req.getString("password");
    String token = req.getString("token");

    Objects.requireNonNull(username, "When using userpass, the username must be set in the `user-credentials` " +
      "configuration");
    Objects.requireNonNull(password, "When using userpass, the password must be set in the `user-credentials` " +
      "configuration");


    client
      .loginWithUserCredentials(username, password, auth -> manageAuthenticationResult(future, auth));
    return future;
  }

  private Future<Void> loginWithCert() {
    Future<Void> future = Future.future();
    // No validation, certs are configured on the client itself
    client.loginWithCert(auth -> manageAuthenticationResult(future, auth));
    return future;
  }


  private Future<Void> loginWithAppRole() {
    Future<Void> future = Future.future();
    JsonObject req = config.getJsonObject("approle");
    Objects.requireNonNull(req, "When using approle, the `app-role` must be set in the " +
      "configuration");
    String roleId = req.getString("role-id");
    String secretId = req.getString("secret-id");

    Objects.requireNonNull(roleId, "When using approle, the role-id must be set in the `approle` configuration");
    Objects.requireNonNull(secretId, "When using approle, the secret-id must be set in the `approle` configuration");

    client.loginWithAppRole(roleId, secretId, auth -> manageAuthenticationResult(future, auth));
    return future;
  }

  private Future<Void> loginWithToken() {
    Future<Void> future = Future.future();
    JsonObject req = config.getJsonObject("token-request");
    Objects.requireNonNull(req, "When using a token creation policy, the `token-request` must be set in the " +
      "configuration");

    String token = req.getString("token");
    Objects.requireNonNull(req, "When using a token creation policy, the `token-request` must be set in the " +
      "configuration and contains the `token` entry with the original token");
    client
      .setToken(token)
      .createToken(new TokenRequest(req), auth -> manageAuthenticationResult(future, auth));
    return future;
  }

  private void manageAuthenticationResult(Future<Void> future, AsyncResult<Auth> auth) {
    if (auth.failed()) {
      future.fail(auth.cause());
    } else {
      Auth authentication = auth.result();
      if (authentication.getToken() == null) {
        future.fail("Authentication failed, the token is null");
      } else {
        client.setToken(authentication.getToken());
        this.renewable = authentication.isRenewable();
        this.validity = System.currentTimeMillis() + (authentication.getLeaseDuration() * 1000);
        future.complete();
      }
    }
  }

  /**
   * Checks whether the token must be renewed
   *
   * @return true if the token is not valid. To avoid timing issue an error margin of 1 minute is added.
   */
  private boolean shouldBeRenewed() {
    long now = System.currentTimeMillis();
    long margin = config.getLong("renew-window", 60000L);
    return now >= validity - margin;
  }

  /**
   * @return the underlying client - for testing purpose only.
   */
  public SlimVaultClient getVaultClient() {
    return client;
  }
}

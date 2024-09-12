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

import io.vertx.config.spi.ConfigStore;
import io.vertx.config.vault.client.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.internal.VertxInternal;
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
  private final VertxInternal vertx;

  /**
   * When authenticated, whether or not the token is renewable.
   */
  private boolean renewable;

  /**
   * The time when the token must be renewed.
   */
  private long validity;

  /**
   * Creates an instance of {@link VaultConfigStore}.
   *
   * @param vertx  the vert.x instance
   * @param config the configuration, used to create the {@link SlimVaultClient}.
   */
  public VaultConfigStore(Vertx vertx, JsonObject config) {
    client = new SlimVaultClient(vertx, config);
    this.config = config;
    this.vertx = (VertxInternal) vertx;
    this.path = Objects.requireNonNull(config.getString("path"), "The path of the secret must be set");
  }

  @Override
  public Future<Void> close() {
    client.close();
    return vertx.getOrCreateContext().succeededFuture();
  }

  @Override
  public Future<Buffer> get() {
    return authenticate(false) // Are we logged in
      .compose(v -> renew()) // Do we need to renew
      .compose(v -> retrieve()) // Retrieve the data
      .compose(this::extract); // Extract the sub set
  }

  private Future<Buffer> extract(JsonObject json) {
    Promise<Buffer> promise = Promise.promise();
    if (json == null) {
      promise.complete(new JsonObject().toBuffer());
    } else if (config.getString("key") != null) {
      Object value = json.getValue(config.getString("key"));
      if (value == null) {
        promise.complete(new JsonObject().toBuffer());
      } else if (value instanceof String) {
        promise.complete(Buffer.buffer((String) value));
      } else if (value instanceof JsonObject) {
        promise.complete(((JsonObject) value).toBuffer());
      }
    } else {
      promise.complete(json.toBuffer());
    }
    return promise.future();
  }

  private Future<JsonObject> retrieve() {
    Promise<Secret> promise = vertx.promise();
    client.read(path, promise);
    return promise.future().map(result -> {
      JsonObject copy = result.getData().copy();
      copy.put("vault-lease-id", result.getLeaseId());
      copy.put("vault-lease-duration", result.getLeaseDuration());
      copy.put("vault-renewable", result.isRenewable());
      return copy;
    }).recover(throwable -> {
      if (throwable instanceof VaultException) {
        VaultException vaultException = (VaultException) throwable;
        if (vaultException.getStatusCode() == 404) {
          return Future.succeededFuture();
        }
      }
      return Future.failedFuture(throwable);
    });
  }

  private Future<Void> renew() {
    Promise<Void> promise = vertx.promise();
    if (validity == 0) {
      // Using a root token - should not expire
      promise.complete();
    } else {
      if (shouldBeRenewed() && renewable) {
        return renewToken();
      } else if (shouldBeRenewed()) {
        // The token cannot be renewed, attempt to authenticate again
        return authenticate(true);
      } else {
        promise.complete();
      }
    }
    return promise.future();
  }

  private Future<Void> renewToken() {
    return client.renewSelf(config.getLong("lease-duration", 3600L))
      .compose(this::manageAuthenticationResult);
  }


  private Future<Void> authenticate(boolean renew) {
    Promise<Void> promise = vertx.promise();
    if (!renew && client.getToken() != null) {
      promise.complete();
      return promise.future();
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


    return client.loginWithUserCredentials(username, password).compose(auth -> manageAuthenticationResult(auth));
  }

  private Future<Void> loginWithCert() {
    // No validation, certs are configured on the client itself
    return client.loginWithCert().compose(auth -> manageAuthenticationResult(auth));
  }


  private Future<Void> loginWithAppRole() {
    JsonObject req = config.getJsonObject("approle");
    Objects.requireNonNull(req, "When using approle, the `app-role` must be set in the " +
      "configuration");
    String roleId = req.getString("role-id");
    String secretId = req.getString("secret-id");

    Objects.requireNonNull(roleId, "When using approle, the role-id must be set in the `approle` configuration");
    Objects.requireNonNull(secretId, "When using approle, the secret-id must be set in the `approle` configuration");

    return client.loginWithAppRole(roleId, secretId).compose(auth -> manageAuthenticationResult(auth));
  }

  private Future<Void> loginWithToken() {
    JsonObject req = config.getJsonObject("token-request");
    Objects.requireNonNull(req, "When using a token creation policy, the `token-request` must be set in the " +
      "configuration");

    String token = req.getString("token");
    Objects.requireNonNull(req, "When using a token creation policy, the `token-request` must be set in the " +
      "configuration and contains the `token` entry with the original token");
    return client
      .setToken(token)
      .createToken(new TokenRequest(req)).compose(auth -> manageAuthenticationResult(auth));
  }

  private Future<Void> manageAuthenticationResult(Auth authentication) {
    if (authentication.getClientToken() == null) {
      return Future.failedFuture("Authentication failed, the token is null");
    } else {
      client.setToken(authentication.getClientToken());
      this.renewable = authentication.isRenewable();
      this.validity = System.currentTimeMillis() + (authentication.getLeaseDuration() * 1000);
      return Future.succeededFuture();
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

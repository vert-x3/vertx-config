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

package io.vertx.config.kubernetes;

import io.vertx.config.spi.ConfigStore;
import io.vertx.config.spi.utils.JsonObjectHelper;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * An implementation of configuration store reading config map from Kubernetes.
 */
public class ConfigMapStore implements ConfigStore {
  private static final String KUBERNETES_NAMESPACE = System.getenv("KUBERNETES_NAMESPACE");
  private static final Base64.Decoder DECODER = Base64.getDecoder();
  private final Vertx vertx;
  private final JsonObject configuration;
  private final String namespace;
  private final String name;
  private final String key;
  private final boolean secret;
  private final boolean optional;

  private final Context ctx;
  private final WebClient client;
  private String token;


  public ConfigMapStore(Vertx vertx, JsonObject configuration) {
    this.vertx = vertx;
    this.configuration = configuration;
    this.ctx = vertx.getOrCreateContext();

    String ns = configuration.getString("namespace");
    if (ns == null) {
      if (KUBERNETES_NAMESPACE != null) {
        ns = KUBERNETES_NAMESPACE;
      } else {
        ns = "default";
      }
    }
    this.optional = configuration.getBoolean("optional", true);
    this.namespace = ns;
    this.name = configuration.getString("name");
    this.key = configuration.getString("key");
    this.secret = configuration.getBoolean("secret", false);
    int port = configuration.getInteger("port", 0);
    if (port == 0) {
      if (configuration.getBoolean("ssl", true)) {
        port = 443;
      } else {
        port = 80;
      }
    }

    String p = System.getenv("KUBERNETES_SERVICE_PORT");
    if (p != null) {
      port = Integer.valueOf(p);
    }

    String host = configuration.getString("host");
    String h = System.getenv("KUBERNETES_SERVICE_HOST");
    if (h != null) {
      host = h;
    }

    client = WebClient.create(vertx,
      new WebClientOptions()
        .setTrustAll(true)
        .setSsl(configuration.getBoolean("ssl", true))
        .setDefaultHost(host)
        .setDefaultPort(port)
        .setFollowRedirects(true)
    );

    Objects.requireNonNull(this.name);
  }

  @Override
  public synchronized void close(Handler<Void> completionHandler) {
    runOnContext(v -> closeOnContext(completionHandler));
  }

  private synchronized void closeOnContext(Handler<Void> completionHandler) {
    if (client != null) {
      client.close();
    }
    if (completionHandler != null) {
      completionHandler.handle(null);
    }
  }

  private void runOnContext(Handler<Void> action) {
    if (Vertx.currentContext() == this.ctx) {
      action.handle(null);
    }
    else {
      ctx.runOnContext(action);
    }
  }

  private Future<String> getToken() {
    Future<String> result = Future.future();

    String token = configuration.getString("token");
    if (token != null && !token.trim().isEmpty()) {
      this.token = token;
      result.complete(token);
      return result;
    }

    // Read from file
    vertx.fileSystem().readFile(KubernetesUtils.OPENSHIFT_KUBERNETES_TOKEN_FILE, ar -> {
      if (ar.failed()) {
        if (optional) {
          this.token = "";
          result.tryComplete(this.token);
        } else {
          result.tryFail(ar.cause());
        }
      } else {
        this.token = ar.result().toString();
        result.tryComplete(ar.result().toString());
      }
    });

    return result;
  }

  @Override
  public void get(Handler<AsyncResult<Buffer>> completionHandler) {
    runOnContext(v -> getOnContext(completionHandler));
  }

  private synchronized void getOnContext(Handler<AsyncResult<Buffer>> completionHandler) {
    Future<String> retrieveToken;
    if (token == null) {
      retrieveToken = getToken();
    } else {
      retrieveToken = Future.succeededFuture(token);
    }

    retrieveToken
      .compose(token -> {
        Future<Buffer> future = Future.future();
        if (token.isEmpty()) {
          future.complete(Buffer.buffer("{}"));
          return future;
        }

        String path = "/api/v1/namespaces/" + namespace;
        if (secret) {
          path += "/secrets/" + name;
        } else {
          path += "/configmaps/" + name;
        }

        client.get(path)
          .putHeader("Authorization", "Bearer " + token)
          .send(ar -> {
            if (ar.failed()) {
              completionHandler.handle(ar.mapEmpty());
              return;
            }
            HttpResponse<Buffer> response = ar.result();
            if (response.statusCode() == 404) {
              if (optional) {
                future.complete(Buffer.buffer("{}"));
              } else {
                future.fail("Cannot find the config map '" + name + "' in '" + namespace + "'");
              }
            } else if (response.statusCode() == 403) {
              completionHandler.handle(Future.failedFuture("Access denied to configmap or secret in namespace "
                + namespace + ": " + name));
            } else if (response.statusCode() != 200) {
              if (optional) {
                future.complete(Buffer.buffer("{}"));
              } else {
                completionHandler.handle(Future.failedFuture("Cannot retrieve the configmap or secret in namespace "
                  + namespace + ": " + name + ", status code: " + response.statusCode() + ", error: "
                  + response.bodyAsString()));
              }
            } else {
              JsonObject data = response.bodyAsJsonObject().getJsonObject("data");
              if (data == null) {
                future.fail("Invalid secret of configmap in namespace " + namespace + " " + name + ", the data " +
                  "entry is empty");
                return;
              }
              if (this.key == null) {
                if (secret) {
                  future.complete(new JsonObject(asSecretObjectMap(data.getMap())).toBuffer());
                }
                else {
                  future.complete(new JsonObject(asObjectMap(data.getMap())).toBuffer());
                }

              } else {
                String string = data.getString(this.key);
                if (string == null) {
                  future.fail("Cannot find key '" + this.key + "' in the configmap or secret '" + this.name + "'");
                } else {
                  if (secret) {
                    future.complete(Buffer.buffer(DECODER.decode(string)));
                  }
                  else {
                    future.complete(Buffer.buffer(string));
                  }
                }
              }
            }
          });
        return future;
      }).setHandler(completionHandler);
  }

  private static Map<String, Object> asObjectMap(Map<String, Object> source) {
    if (source == null) {
      return new HashMap<>();
    }
    return source.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
      entry -> JsonObjectHelper.convert(entry.getValue().toString())));
  }


  private static Map<String, Object> asSecretObjectMap(Map<String, Object> source) {
    if (source == null) {
      return new HashMap<>();
    }
    return source.entrySet().stream()
      .collect(Collectors.toMap(Map.Entry::getKey,
        entry -> {
          String encodedString = entry.getValue().toString();
          String decodedString = new String(DECODER.decode(encodedString), UTF_8);
          return JsonObjectHelper.convert(decodedString);
        }));
  }
}

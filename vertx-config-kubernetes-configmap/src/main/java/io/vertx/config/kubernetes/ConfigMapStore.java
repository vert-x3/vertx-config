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
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.VertxInternal;
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
  private final VertxInternal vertx;
  private final JsonObject configuration;
  private final String name;
  private final String key;
  private final boolean secret;
  private final boolean optional;

  private final WebClient client;
  private String namespace;
  private String token;


  public ConfigMapStore(Vertx vertx, JsonObject configuration) {
    this.vertx = (VertxInternal) vertx;
    this.configuration = configuration;

    this.optional = configuration.getBoolean("optional", true);
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
  public Future<Void> close() {
    if (client != null) {
      client.close();
    }
    return vertx.getOrCreateContext().succeededFuture();
  }

  private Future<String> getToken() {
    String token = configuration.getString("token");
    if (token != null && !token.trim().isEmpty()) {
      this.token = token;
      return vertx.getOrCreateContext().succeededFuture(token);
    }

    // Read from file
    return vertx.fileSystem().readFile(KubernetesUtils.OPENSHIFT_KUBERNETES_TOKEN_FILE)
      .recover(throwable -> optional ? Future.succeededFuture(Buffer.buffer()) : Future.failedFuture(throwable))
      .map(Buffer::toString)
      .onSuccess(tk -> {
        this.token = tk;
      });
  }

  private Future<String> getNamespace() {
    String namespace = configuration.getString("namespace");
    if (namespace != null && !namespace.trim().isEmpty()) {
      this.namespace = namespace;
      return vertx.getOrCreateContext().succeededFuture(namespace);
    }

    if (KUBERNETES_NAMESPACE != null) {
      this.namespace = KUBERNETES_NAMESPACE;
      return vertx.getOrCreateContext().succeededFuture(namespace);
    }

    // Read from file
    return vertx.fileSystem().readFile(KubernetesUtils.OPENSHIFT_KUBERNETES_NAMESPACE_FILE)
      .recover(throwable -> optional ? Future.succeededFuture(Buffer.buffer()) : Future.failedFuture(throwable))
      .map(Buffer::toString)
      .onSuccess(ns -> {
          this.namespace = ns;
        })
      .onFailure(t->{
          this.namespace = "default";
        });
  }

  @Override
  public Future<Buffer> get() {
    Future<String> retrieveToken;
    if (token == null) {
      retrieveToken = getToken();
    } else {
      retrieveToken = vertx.getOrCreateContext().succeededFuture(token);
    }

    Future<String> retrieveNamespace;
    if (namespace == null) {
      retrieveNamespace = getNamespace();
    } else {
      retrieveNamespace = vertx.getOrCreateContext().succeededFuture(namespace);
    }

    return CompositeFuture.all(retrieveToken, retrieveNamespace).flatMap(compFut->{
        String token = compFut.resultAt(0);
        String namespace = compFut.resultAt(1);

        if (token.isEmpty()) {
          return Future.succeededFuture(Buffer.buffer("{}"));
        }

        String path = "/api/v1/namespaces/" + namespace;
        if (secret) {
          path += "/secrets/" + name;
        } else {
          path += "/configmaps/" + name;
        }

        return client.get(path)
          .putHeader("Authorization", "Bearer " + token)
          .send()
          .flatMap(response -> {
              if (response.statusCode() == 404) {
                return handle404();
              }
              if (response.statusCode() == 403) {
                return handle403();
              }
              if (response.statusCode() != 200) {
                return handleOtherErrors(response);
              }
              return handle200(response);
            });

      });
  }

  private Future<Buffer> handle404() {
    if (optional) {
      return Future.succeededFuture(Buffer.buffer("{}"));
    }
    return Future.failedFuture("Cannot find the config map '" + name + "' in '" + namespace + "'");
  }

  private Future<Buffer> handle403() {
    return Future.failedFuture("Access denied to configmap or secret in namespace " + namespace + ": " + name);
  }

  private Future<Buffer> handleOtherErrors(HttpResponse<Buffer> response) {
    if (optional) {
      return Future.succeededFuture(Buffer.buffer("{}"));
    }
    return Future.failedFuture("Cannot retrieve the configmap or secret in namespace "
      + namespace + ": " + name + ", status code: " + response.statusCode() + ", error: "
      + response.bodyAsString());
  }

  private Future<Buffer> handle200(HttpResponse<Buffer> response) {
    JsonObject data = response.bodyAsJsonObject().getJsonObject("data");
    if (data == null) {
      return Future.failedFuture("Invalid secret of configmap in namespace " + namespace + " " + name
        + ", the data " + "entry is empty");
    }
    if (this.key == null) {
      if (secret) {
        return Future.succeededFuture(new JsonObject(asSecretObjectMap(data.getMap())).toBuffer());
      }
      return Future.succeededFuture(new JsonObject(asObjectMap(data.getMap())).toBuffer());
    }
    String string = data.getString(this.key);
    if (string == null) {
      return Future.failedFuture("Cannot find key '" + this.key + "' in the configmap or secret '" + this.name + "'");
    }
    if (secret) {
      return Future.succeededFuture(Buffer.buffer(DECODER.decode(string)));
    }
    return Future.succeededFuture(Buffer.buffer(string));
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

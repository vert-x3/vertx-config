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

package io.vertx.config.spring;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.config.spi.ConfigStore;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.Objects;

/**
 * Retrieves configuration from Spring Config Servers.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
class SpringConfigServerStore implements ConfigStore {


  private final String path;
  private final String authHeaderValue;
  private final HttpClient client;
  private final long timeout;

  SpringConfigServerStore(Vertx vertx, JsonObject configuration) {
    String url = configuration.getString("url");
    this.timeout = configuration.getLong("timeout", 3000L);
    Objects.requireNonNull(url);

    HttpClientOptions options = new HttpClientOptions(
      configuration.getJsonObject("httpClientConfiguration", new JsonObject()));
    try {
      URL u = new URL(url);
      options.setDefaultHost(u.getHost());
      if (u.getPort() == -1) {
        options.setDefaultPort(u.getDefaultPort());
      } else {
        options.setDefaultPort(u.getPort());
      }

      if (u.getPath() != null) {
        path = u.getPath();
      } else {
        path = "/";
      }

    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("Invalid url for the spring server: " + url);
    }


    if (configuration.getString("user") != null && configuration.getString("password") != null) {
      authHeaderValue = "Basic " + Base64.getEncoder().encodeToString((configuration.getString("user")
          + ":" + configuration.getString("password")).getBytes());
    } else {
      authHeaderValue = null;
    }

    client = vertx.createHttpClient(options);

  }

  @Override
  public void close(Handler<Void> completionHandler) {
    if (client != null) {
      client.close();
    }
    completionHandler.handle(null);
  }

  @Override
  public void get(Handler<AsyncResult<Buffer>> completionHandler) {
    HttpClientRequest request = client.get(path, response -> {
      if (response.statusCode() != 200) {
        completionHandler.handle(Future.failedFuture("Invalid response from server: " + response.statusCode() + " - "
            + response.statusMessage()));
      } else {
        response
            .exceptionHandler(t -> completionHandler.handle(Future.failedFuture(t)))
            .bodyHandler(buffer -> parse(buffer.toJsonObject(), completionHandler));
      }
    })
        .setTimeout(timeout)
        .exceptionHandler(t -> completionHandler.handle(Future.failedFuture(t)));

    if (authHeaderValue != null) {
      request.putHeader("Authorization", authHeaderValue);
    }

    request.end();
  }

  private void parse(JsonObject body, Handler<AsyncResult<Buffer>> handler) {
    if (this.path.endsWith(".json")) {
      parseFromJson(body, handler);
    } else {
      parseFromStandard(body, handler);
    }
  }

  private void parseFromStandard(JsonObject body, Handler<AsyncResult<Buffer>> handler) {
    JsonArray sources = body.getJsonArray("propertySources");
    if (sources == null) {
      handler.handle(Future.failedFuture("Invalid configuration server response, property sources missing"));
    } else {
      JsonObject configuration = new JsonObject();
      for (int i = sources.size() - 1; i >= 0; i--) {
        JsonObject source = sources.getJsonObject(i);
        JsonObject content = source.getJsonObject("source");
        configuration = configuration.mergeIn(content);
      }
      handler.handle(Future.succeededFuture(Buffer.buffer(configuration.encode())));
    }
  }

  private void parseFromJson(JsonObject body, Handler<AsyncResult<Buffer>> handler) {
    if (body == null) {
      handler.handle(Future.failedFuture("Invalid configuration server response, property sources missing"));
    } else {
      handler.handle(Future.succeededFuture(Buffer.buffer(body.encode())));
    }
  }
}

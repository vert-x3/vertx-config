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

package io.vertx.config.impl.spi;

import io.vertx.config.spi.ConfigStore;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;

/**
 * A configuration store retrieving the configuration from a HTTP location
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class HttpConfigStore implements ConfigStore {

  private final HttpClient client;
  private final RequestOptions requestOptions;

  public HttpConfigStore(Vertx vertx, JsonObject configuration) {
    String host = configuration.getString("host");
    int port = configuration.getInteger("port", 80);
    String path = configuration.getString("path", "/");
    long timeout = configuration.getLong("timeout", 3000L);
    boolean followRedirects = configuration.getBoolean("followRedirects", false);
    this.client = vertx.createHttpClient(new HttpClientOptions(configuration));
    this.requestOptions = new RequestOptions()
      .setHost(host)
      .setPort(port)
      .setURI(path)
      .setTimeout(timeout)
      .setFollowRedirects(followRedirects);
    configuration.getJsonObject("headers", new JsonObject()).stream()
      .filter(h -> h.getValue() != null)
      .forEach(h -> requestOptions.addHeader(h.getKey(), h.getValue().toString()));
  }

  @Override
  public void get(Handler<AsyncResult<Buffer>> completionHandler) {
    client.get(requestOptions, ar -> {
      if (ar.succeeded()) {
        HttpClientResponse response = ar.result();
        response
          .exceptionHandler(t -> completionHandler.handle(Future.failedFuture(t)))
          .bodyHandler(buffer -> completionHandler.handle(Future.succeededFuture(buffer)));
      } else {
        completionHandler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  @Override
  public void close(Handler<Void> completionHandler) {
    this.client.close();
    completionHandler.handle(null);
  }
}

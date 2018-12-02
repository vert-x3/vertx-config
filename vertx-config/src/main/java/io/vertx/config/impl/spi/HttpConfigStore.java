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
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;

/**
 * A configuration store retrieving the configuration from a HTTP location
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class HttpConfigStore implements ConfigStore {

  private final String host;
  private final int port;
  private final String path;
  private final HttpClient client;

  public HttpConfigStore(String host, int port, String path, HttpClient client) {
    this.host = host;
    this.port = port;
    this.path = path;
    this.client = client;
  }

  @Override
  public void get(Handler<AsyncResult<Buffer>> completionHandler) {
    client.getNow(port, host, path, ar -> {
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

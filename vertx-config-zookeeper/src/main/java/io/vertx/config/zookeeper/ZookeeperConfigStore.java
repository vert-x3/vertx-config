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

package io.vertx.config.zookeeper;

import io.vertx.config.spi.ConfigStore;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.json.JsonObject;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.KeeperException;

import java.util.Objects;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ZookeeperConfigStore implements ConfigStore {

  private final CuratorFramework client;
  private final String path;
  private final VertxInternal vertx;

  public ZookeeperConfigStore(Vertx vertx, JsonObject configuration) {
    String connection = Objects.requireNonNull(configuration.getString("connection"));
    path = Objects.requireNonNull(configuration.getString("path"));
    this.vertx = (VertxInternal) Objects.requireNonNull(vertx);
    int maxRetries = configuration.getInteger("maxRetries", 3);
    int baseGraceBetweenRetries = configuration.getInteger("baseSleepTimeBetweenRetries", 1000);

    client = CuratorFrameworkFactory.newClient(connection,
        new ExponentialBackoffRetry(baseGraceBetweenRetries, maxRetries));
    client.start();
  }

  @Override
  public Future<Buffer> get() {
    return vertx.executeBlocking(() -> {
      client.blockUntilConnected();
      return null;
    }).flatMap(v -> {
      // We are connected.
      Promise<Buffer> promise = vertx.promise();
      try {
        client.getData()
          .inBackground((client, event) -> retrieve(event, promise))
          .withUnhandledErrorListener((message, e) -> promise.fail(new Exception(message, e)))
          .forPath(path);
      } catch (Exception e) {
        promise.fail(e);
      }
      return promise.future();
    });
  }

  private void retrieve(CuratorEvent event, Promise<Buffer> promise) {
    KeeperException.Code code = KeeperException.Code.get(event.getResultCode());
    if (code == KeeperException.Code.OK) {
      promise.complete(Buffer.buffer(event.getData()));
    } else if (code == KeeperException.Code.NONODE) {
      promise.complete(Buffer.buffer("{}"));
    } else {
      promise.fail(KeeperException.create(code, path));
    }
  }

  @Override
  public Future<Void> close() {
    client.close();
    return vertx.getOrCreateContext().succeededFuture();
  }

}

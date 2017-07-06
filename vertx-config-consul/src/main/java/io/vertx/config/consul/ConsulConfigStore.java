/*
 * Copyright (c) 2017 The original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *      The Eclipse Public License is available at
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *      The Apache License v2.0 is available at
 *      http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.config.consul;

import io.vertx.config.spi.ConfigStore;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.consul.ConsulClient;
import io.vertx.ext.consul.ConsulClientOptions;
import io.vertx.ext.consul.KeyValue;
import io.vertx.ext.consul.KeyValueList;

/**
 * @author <a href="mailto:ruslan.sennov@gmail.com">Ruslan Sennov</a>
 */
public class ConsulConfigStore implements ConfigStore {

  private final ConsulClient client;
  private final String delimiter;
  private final String prefix;

  ConsulConfigStore(Vertx vertx, JsonObject configuration) {
    client = ConsulClient.create(vertx, new ConsulClientOptions(configuration));
    delimiter = configuration.getString("delimiter", "/");
    prefix = prefix(configuration.getString("prefix"), delimiter);
  }

  @Override
  public void get(Handler<AsyncResult<Buffer>> completionHandler) {
    client.getValues(prefix, kv -> {
      if (kv.succeeded()) {
        JsonObject tree = getTree(kv.result(), prefix.length(), delimiter);
        completionHandler.handle(Future.succeededFuture(Buffer.buffer(tree.toString())));
      } else {
        String message = kv.cause().getMessage();
        if ("not found".equals(message.toLowerCase())) {
          completionHandler.handle(Future.succeededFuture(Buffer.buffer("{}")));
        } else {
          completionHandler.handle(Future.failedFuture(kv.cause()));
        }
      }
    });
  }

  @Override
  public void close(Handler<Void> completionHandler) {
    client.close();
    completionHandler.handle(null);
  }

  private static JsonObject getTree(KeyValueList list, int prefix, String delimiter) {
    JsonObject tree = new JsonObject();
    for (KeyValue keyValue : list.getList()) {
      if (keyValue.getKey().endsWith(delimiter)) {
        continue;
      }
      JsonObject json = tree;
      String[] arr = keyValue.getKey().substring(prefix).split(delimiter);
      for (int i = 0; i < arr.length; i++) {
        String key = arr[i];
        if (i == arr.length - 1) {
          json.put(key, keyValue.getValue());
        } else {
          JsonObject next = json.getJsonObject(key);
          if (next == null) {
            next = new JsonObject();
            json.put(key, next);
          }
          json = next;
        }
      }
    }
    return tree;
  }

  private static String prefix(String prefix, String delimiter) {
    if (prefix == null || prefix.isEmpty()) {
      return "";
    } else {
      return prefix.endsWith(delimiter) ? prefix : prefix + delimiter;
    }
  }
}

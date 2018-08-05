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

import io.vertx.config.spi.ConfigProcessor;
import io.vertx.config.spi.utils.JsonObjectHelper;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.util.Properties;

/**
 * Transforms properties to json.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class PropertiesConfigProcessor implements ConfigProcessor {

  @Override
  public String name() {
    return "properties";
  }

  @Override
  public void process(Vertx vertx, JsonObject configuration, Buffer input, Handler<AsyncResult<JsonObject>> handler) {
    // I'm not sure the executeBlocking is really required here as the
    // buffer is in memory,
    // so the input stream is not blocking
    vertx.executeBlocking(future -> {
      byte[] bytes = input.getBytes();
      ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
      Properties properties = new Properties();
      try {
        properties.load(stream);
        JsonObject created = JsonObjectHelper.from(properties, configuration.getBoolean("raw-data", false));
        future.complete(created);
      } catch (Exception e) {
        future.fail(e);
      } finally {
        closeQuietly(stream);
      }
    }, handler);
  }

  public static void closeQuietly(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (Exception e) {
        // Ignore it.
      }
    }
  }
}

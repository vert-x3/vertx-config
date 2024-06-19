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

package io.vertx.config.impl;

import io.vertx.config.spi.ConfigProcessor;
import io.vertx.config.spi.ConfigStore;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.internal.logging.Logger;
import io.vertx.core.internal.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;

/**
 * A configuration provider retrieve the configuration from a store and transform it to Json.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ConfigurationProvider {

  private final JsonObject configuration;

  private final boolean optional;

  private final ConfigStore store;

  private final ConfigProcessor processor;

  private final Logger logger;

  public ConfigurationProvider(ConfigStore store, ConfigProcessor processor, JsonObject config, boolean optional) {
    this.store = store;
    this.processor = processor;
    this.optional = optional;
    if (config == null) {
      this.configuration = new JsonObject();
    } else {
      this.configuration = config;
    }
    this.logger = LoggerFactory.getLogger("ConfigurationProvider#" + store);
  }

  Future<JsonObject> get(Vertx vertx) {
    return store.get()
      .onFailure(throwable -> {
        if (optional && logger.isDebugEnabled()) {
          logger.debug("Unable to retrieve the configuration", throwable);
        }
      })
      .flatMap(maybeBuffer -> {
        return processor.process(vertx, configuration, maybeBuffer)
          .onFailure(throwable -> {
            if (optional) {
              if (logger.isDebugEnabled()) {
                logger.debug("Failure caught when processing the configuration", throwable);
              }
            }
          });
      }).recover(throwable -> optional ? Future.succeededFuture(new JsonObject()) : Future.failedFuture(throwable));
  }

  void close() {
    store.close();
  }

  public ConfigStore getStore() {
    return store;
  }

  public ConfigProcessor getProcessor() {
    return processor;
  }
}

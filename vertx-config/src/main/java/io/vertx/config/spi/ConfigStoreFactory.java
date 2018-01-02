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

package io.vertx.config.spi;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Factory to create instances of {@link ConfigStore}. This is a SPI, and so implementations are retrieved
 * from the classpath / classloader using a {@link java.util.ServiceLoader}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public interface ConfigStoreFactory {

  /**
   * @return the name of the factory.
   */
  String name();

  /**
   * Creates an instance of the {@link ConfigStore}.
   *
   * @param vertx         the vert.x instance, never {@code null}
   * @param configuration the configuration, never {@code null}, but potentially empty
   * @return the created configuration store
   */
  ConfigStore create(Vertx vertx, JsonObject configuration);

}

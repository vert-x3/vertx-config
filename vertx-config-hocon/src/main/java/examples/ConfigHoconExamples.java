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

package examples;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ConfigHoconExamples {


  public void example1(Vertx vertx) {
    ConfigStoreOptions store = new ConfigStoreOptions()
      .setType("file")
      .setFormat("hocon")
      .setConfig(new JsonObject()
        .put("path", "my-config.conf")
      );

    ConfigRetriever retriever = ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions().addStore(store));
  }

  public void exampleWithEnvOverrides(Vertx vertx) {
    ConfigStoreOptions store = new ConfigStoreOptions()
      .setType("file")
      .setFormat("hocon")
      .setConfig(new JsonObject()
        .put("hocon.env.override", true)
        .put("path", "my-config.conf")
      );

    ConfigRetriever retriever = ConfigRetriever.create(vertx,
      new ConfigRetrieverOptions().addStore(store));
  }

}

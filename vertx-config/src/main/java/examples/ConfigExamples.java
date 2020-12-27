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
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ConfigExamples {


  public void example1(Vertx vertx) {
    ConfigRetriever retriever = ConfigRetriever.create(vertx);
  }

  public void example2(Vertx vertx) {
    ConfigStoreOptions httpStore = new ConfigStoreOptions()
      .setType("http")
      .setConfig(new JsonObject()
        .put("host", "localhost").put("port", 8080).put("path", "/conf"));

    ConfigStoreOptions fileStore = new ConfigStoreOptions()
      .setType("file")
      .setConfig(new JsonObject().put("path", "my-config.json"));

    ConfigStoreOptions sysPropsStore = new ConfigStoreOptions().setType("sys");


    ConfigRetrieverOptions options = new ConfigRetrieverOptions()
      .addStore(httpStore).addStore(fileStore).addStore(sysPropsStore);

    ConfigRetriever retriever = ConfigRetriever.create(vertx, options);
  }

  public void example2_optional(Vertx vertx) {
    ConfigStoreOptions fileStore = new ConfigStoreOptions()
      .setType("file")
      .setOptional(true)
      .setConfig(new JsonObject().put("path", "my-config.json"));
    ConfigStoreOptions sysPropsStore = new ConfigStoreOptions().setType("sys");

    ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(fileStore).addStore(sysPropsStore);

    ConfigRetriever retriever = ConfigRetriever.create(vertx, options);
  }

  public void example3(ConfigRetriever retriever) {
    retriever.getConfig(ar -> {
      if (ar.failed()) {
        // Failed to retrieve the configuration
      } else {
        JsonObject config = ar.result();
      }
    });
  }

  public void future(ConfigRetriever retriever) {
    Future<JsonObject> future = retriever.getConfig();
    future.onComplete(ar -> {
      if (ar.failed()) {
        // Failed to retrieve the configuration
      } else {
        JsonObject config = ar.result();
      }
    });
  }

  public void file() {
    ConfigStoreOptions file = new ConfigStoreOptions()
      .setType("file")
      .setFormat("properties")
      .setConfig(new JsonObject().put("path", "path-to-file.properties"));
  }

  public void json() {
    ConfigStoreOptions json = new ConfigStoreOptions()
      .setType("json")
      .setConfig(new JsonObject().put("key", "value"));
  }

  public void sys() {
    ConfigStoreOptions json = new ConfigStoreOptions()
      .setType("sys")
      .setConfig(new JsonObject().put("cache", false));
  }

  public void env() {
    ConfigStoreOptions json = new ConfigStoreOptions()
      .setType("env");
  }

  public void env2() {
    ConfigStoreOptions json = new ConfigStoreOptions()
      .setType("env")
      .setConfig(new JsonObject().put("raw-data", true));
  }

  public void env3() {
    ConfigStoreOptions json = new ConfigStoreOptions()
      .setType("env")
      .setConfig(new JsonObject().put("keys", new JsonArray().add("SERVICE1_HOST").add("SERVICE2_HOST")));
  }


  public void http() {
    ConfigStoreOptions http = new ConfigStoreOptions()
      .setType("http")
      .setConfig(new JsonObject()
        .put("host", "localhost")
        .put("port", 8080)
        .put("path", "/A"));
  }

  public void http2() {
    ConfigStoreOptions http = new ConfigStoreOptions()
      .setType("http")
      .setConfig(new JsonObject()
        .put("defaultHost", "localhost")
        .put("defaultPort", 8080)
        .put("ssl", true)
        .put("path", "/A")
        .put("headers", new JsonObject().put("Accept", "application/json")));
  }

  public void eb() {
    ConfigStoreOptions eb = new ConfigStoreOptions()
      .setType("event-bus")
      .setConfig(new JsonObject()
        .put("address", "address-getting-the-conf")
      );
  }

  public void dir() {
    ConfigStoreOptions dir = new ConfigStoreOptions()
      .setType("directory")
      .setConfig(new JsonObject().put("path", "config")
        .put("filesets", new JsonArray()
          .add(new JsonObject().put("pattern", "dir/*json"))
          .add(new JsonObject().put("pattern", "dir/*.properties")
            .put("format", "properties"))
        ));

    ConfigStoreOptions dirWithRawData = new ConfigStoreOptions()
      .setType("directory")
      .setConfig(new JsonObject().put("path", "config")
        .put("filesets", new JsonArray()
          .add(new JsonObject().put("pattern", "dir/*json"))
          .add(new JsonObject().put("pattern", "dir/*.properties")
            .put("format", "properties").put("raw-data", true))
        ));
  }

  public void propsWithRawData() {
    ConfigStoreOptions propertyWithRawData = new ConfigStoreOptions()
      .setFormat("properties")
      .setType("file")
      .setConfig(new JsonObject().put("path", "raw.properties").put("raw-data", true)
      );
  }

  public void propsWitHierarchicalStructure() {
    ConfigStoreOptions propertyWitHierarchical = new ConfigStoreOptions()
      .setFormat("properties")
      .setType("file")
      .setConfig(new JsonObject().put("path", "hierarchical.properties").put("hierarchical", true)
      );
    ConfigRetrieverOptions options = new ConfigRetrieverOptions()
      .addStore(propertyWitHierarchical);

    ConfigRetriever configRetriever = ConfigRetriever.create(Vertx.vertx(), options);

    configRetriever.configStream().handler(config -> {
      String host = config.getJsonObject("server").getString("host");
      Integer port = config.getJsonObject("server").getInteger("port");
      JsonArray multiple = config.getJsonObject("multiple").getJsonArray("values");
      for (int i = 0; i < multiple.size(); i++) {
        Integer value = multiple.getInteger(i);
      }
    });
  }

  public void consul() {
    ConfigStoreOptions consul = new ConfigStoreOptions()
      .setType("consul")
      .setConfig(new JsonObject()
        .put("prefix", "foo"));
  }

  public void period(ConfigStoreOptions store1, ConfigStoreOptions store2) {
    ConfigRetrieverOptions options = new ConfigRetrieverOptions()
      .setScanPeriod(2000)
      .addStore(store1)
      .addStore(store2);

    ConfigRetriever retriever = ConfigRetriever.create(Vertx.vertx(), options);
    retriever.getConfig(json -> {
      // Initial retrieval of the configuration
    });

    retriever.listen(change -> {
      // Previous configuration
      JsonObject previous = change.getPreviousConfiguration();
      // New configuration
      JsonObject conf = change.getNewConfiguration();
    });
  }

  public void stream(ConfigStoreOptions store1, ConfigStoreOptions store2) {
    ConfigRetrieverOptions options = new ConfigRetrieverOptions()
      .setScanPeriod(2000)
      .addStore(store1)
      .addStore(store2);

    ConfigRetriever retriever = ConfigRetriever.create(Vertx.vertx(), options);
    retriever.configStream()
      .endHandler(v -> {
        // retriever closed
      })
      .exceptionHandler(t -> {
        // an error has been caught while retrieving the configuration
      })
      .handler(conf -> {
        // the configuration
      });

  }

  public void cache(ConfigRetriever retriever) {
    JsonObject last = retriever.getCachedConfig();
  }

}

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

/**
 * = Vert.x Config
 *
 * Vert.x Config provides a way to configure your Vert.x application.
 * It:
 *
 * * offers multiple configuration syntaxes (json, properties, yaml (extension), hocon
 * (extension)...
 * * offers multiple configuration stores (files, directories, http, git (extension), redis
 * (extension), system properties, environment properties)...
 * * lets you define the processing order and overloading
 * * supports runtime reconfiguration
 *
 * == Concepts
 *
 * The library is structured around:
 *
 * * a **Config Retriever** instantiated and used by the Vert.x application. It
 * configures a set of configuration store
 * * **Configuration store** defines a location from where the configuration data is read
 * and and a syntax (json by default)
 *
 * The configuration is retrieved as a JSON Object.
 *
 * == Using the Config Retriever
 *
 * To use the Config Retriever, add the following dependency to the
 * _dependencies_ section of your build descriptor:
 *
 * * Maven (in your `pom.xml`):
 *
 * [source,xml,subs="+attributes"]
 * ----
 * <dependency>
 *   <groupId>${maven.groupId}</groupId>
 *   <artifactId>${maven.artifactId}</artifactId>
 *   <version>${maven.version}</version>
 * </dependency>
 * ----
 *
 * * Gradle (in your `build.gradle` file):
 *
 * [source,groovy,subs="+attributes"]
 * ----
 * compile '${maven.groupId}:${maven.artifactId}:${maven.version}'
 * ----
 *
 * Once done, you first need to instantiate the {@link io.vertx.config.ConfigRetriever}:
 *
 * [source]
 * ----
 * {@link examples.Examples#example1(io.vertx.core.Vertx)}
 * ----
 *
 * By default the Config Retriever is configured with the following stores (in
 * this order):
 *
 * * The Vert.x verticle `config()`
 * * The system properties
 * * The environment variables
 *
 *
 * You can configure your own stores:
 *
 * [source]
 * ----
 * {@link examples.Examples#example2(io.vertx.core.Vertx)}
 * ----
 *
 * More details about the overloading rules and available stores are available below. Each store can be marked as
 * `optional`. If a failure is caught while retrieving (or processing) the configuration from an optional store, the failure
 * is logged but the processing does not fail. Instead an empty JSON object is returned (`{}`). To mark a store as
 * optional, use the `optional` attribute:
 *
 * [source]
 * ----
 * {@link examples.Examples#example2_optional(io.vertx.core.Vertx)}
 * ----
 *
 * Once you have the instance of the Config Retriever, _retrieve_ the configuration
 * as follows:
 *
 * [source]
 * ----
 * {@link examples.Examples#example3(ConfigRetriever)}
 * ----
 *
 * == Overloading rules
 *
 * The declaration order of the configuration store is important as it defines the
 * overloading. For conflicting key, configuration stores arriving _last_ overloads the
 * value provided by the previous configuration stores. Let's take an example. We have 2
 * configuration stores:
 *
 * * `A` provides `{a:value, b:1}`
 * * `B` provides `{a:value2, c:2}`
 *
 * Declared in this order (A, B), the resulting configuration would be:
 * `{a:value2, b:1, c:2}`.
 *
 * If you declare them in the reverse order (B, A), you would get: * `{a:value, b:1, c:2}`.
 *
 * == Available configuration stores
 *
 * The Config Retriever provides a set of configuration store and format.
 * Some more are available as extension and you can implement your own.
 *
 * === Structure of the configuration
 *
 * Each declared data store must specify the `type`. It can also define the `format`. If
 * not set JSON is used.
 *
 * Some configurations tore requires additional configuration (such a path...). This
 * configuration is passed as a Json Object using {@link io.vertx.config.ConfigStoreOptions#setConfig(io.vertx.core.json.JsonObject)}
 *
 * === File
 *
 * This configuration store just read the configuration from a file. It supports all
 * supported formats.
 *
 * [source, $lang]
 * ----
 * {@link examples.Examples#file()}
 * ----
 *
 * The `path` configuration is required.
 *
 * === JSON
 *
 * The JSON configuration store just serves the given JSON config as it is.
 *
 * [source, $lang]
 * ----
 * {@link examples.Examples#json()}
 * ----
 *
 * The only supported format for this configuration store is JSON.
 *
 * === Environment Variables
 *
 * This configuration store maps environment variables to a Json Object contributed to
 * the global configuration.
 *
 * [source, $lang]
 * ----
 * {@link examples.Examples#env()}
 * ----
 *
 * This configuration store does not support the `format` configuration. By default, the retrieved value are
 * transformed into JSON compatible structures (number, string, boolean, json object and json array). To avoid this
 * conversion, configure the `raw-data` attribute:
 *
 * [source, $lang]
 * ----
 * {@link examples.Examples#env2()}
 * ----
 *
 * You can configure the `raw-data` attribute (`false` by default). If `raw-data` is `true` no attempts to convert
 * values will be made and you'll be able to get raw values using `config.getString(key)`.
 *
 * If you want to select the set of keys to import, use the `keys` attributes. It filters out all non selected keys. Keys
 * must be listed individually:
 *
 * [source, $lang]
 * ----
 * {@link examples.Examples#env3()}
 * ----
 *
 * === System Properties
 *
 * This configuration store maps system properties to a Json Object contributed to the
 * global configuration.
 *
 * [source, $lang]
 * ----
 * {@link examples.Examples#sys()}
 * ----
 *
 * This configuration store does not support the `format` configuration.
 *
 * You can configure the `cache` attribute (`true` by default) let you decide whether or
 * not it caches the system properties on the first access and does not reload them.
 *
 * === HTTP
 *
 * This configuration stores retrieves the configuration from a HTTP location. It can use
 * any supported format.
 *
 * [source, $lang]
 * ----
 * {@link examples.Examples#http()}
 * ----
 *
 * It creates a Vert.x HTTP Client with the store configuration (see next snippet). To
 * ease the configuration, you can also configure the `host`, `port` and `path` with the
 * `host`, `port` and `path`
 * properties.
 *
 * [source, $lang]
 * ----
 * {@link examples.Examples#http2()}
 * ----
 *
 * === Event Bus
 *
 * This event bus configuration stores receives the configuration from the event bus. This
 * stores let you distribute your configuration among your local and distributed components.
 *
 * [source, $lang]
 * ----
 * {@link examples.Examples#eb()}
 * ----
 *
 * This configuration store supports any type of format.
 *
 * === Directory
 *
 * This configuration store is similar to the `file` configuration store, but instead of
 * reading a single file, read several files from a directory.
 *
 * This configuration store configuration requires:
 *
 * * a `path` - the root directory in which files are located
 * * at least one `fileset` - an object to select the files
 *
 * Each `fileset` contains:
 * * a `pattern` : a Ant style pattern to select files. The pattern is applied on the
 * relative path of the files location in the directory.
 * * an optional `format` indicating the format of the files (each fileset can use a
 * different format, BUT files in a fileset must share the same format).
 *
 * [source, $lang]
 * ----
 * {@link examples.Examples#dir()}
 * ----
 *
 * == Listening for configuration changes
 *
 * The Configuration Retriever periodically retrieve the configuration and if the outcome
 * is different from the current one, your application can be reconfigured. By default the
 * configuration is reloaded every 5 seconds.
 *
 * [source, $lang]
 * ----
 * {@link examples.Examples#period(ConfigStoreOptions, ConfigStoreOptions)}
 * ----
 *
 * == Retrieving the last retrieved configuration
 *
 * You can retrieved the last retrieved configuration without "waiting" to be retrieved
 * using:
 *
 * [source, $lang]
 * ----
 * {@link examples.Examples#cache(ConfigRetriever)}
 * ----
 *
 * == Reading configuration as a stream
 *
 * The {@link io.vertx.config.ConfigRetriever} provide a way to access the stream of configuration.
 * It's a {@link io.vertx.core.streams.ReadStream} of {@link io.vertx.core.json.JsonObject}. By registering the right
 * set of handlers you are notified:
 *
 * * when a new configuration is retrieved
 * * when an error occur while retrieving a configuration
 * * when the configuration retriever is closed (the
 * {@link io.vertx.core.streams.ReadStream#endHandler(io.vertx.core.Handler)} is called).
 *
 * [source, $lang]
 * ----
 * {@link examples.Examples#stream(ConfigStoreOptions, ConfigStoreOptions)}
 * ----
 *
 * == Retrieving the configuration as a Future
 *
 * The {@link io.vertx.config.ConfigRetriever} provide a way to retrieve the configuration as a
 * {@link io.vertx.core.Future}:
 *
 * [source, $lang]
 * ----
 * {@link examples.Examples#future(io.vertx.config.ConfigRetriever)}
 * ----
 *
 * == Extending the Config Retriever
 *
 * You can extend the configuration by implementing:
 *
 * * the {@link io.vertx.config.spi.ConfigProcessor} SPI to add support for a
 * format
 * * the {@link io.vertx.config.spi.ConfigStoreFactory} SPI to add support for
 * configuration store (place from where the configuration data is retrieved)
 *
 * == Additional formats
 *
 * In addition of the out of the box format supported by this library, Vert.x Config provides additional
 * formats you can use in your application.
 *
 * include::hocon-format.adoc[]
 *
 * include::yaml-format.adoc[]
 *
 * == Additional stores
 *
 * In addition of the out of the box stores supported by this library, Vert.x Config provides additional
 * stores you can use in your application.
 *
 * include::git-store.adoc[]
 *
 * include::kubernetes-store.adoc[]
 *
 * include::redis-store.adoc[]
 *
 * include::zookeeper-store.adoc[]
 *
 * include::consul-store.adoc[]
 *
 * include::spring-store.adoc[]
 *
 * include::vault-store.adoc[]
 *
 * include::consul-store.adoc[]
 */
@Document(fileName = "index.adoc")
@ModuleGen(name = "vertx-config", groupPackage = "io.vertx")
package io.vertx.config;

import io.vertx.codegen.annotations.ModuleGen;
import io.vertx.docgen.Document;

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
 * === Zookeeper Configuration Store
 *
 * The Zookeeper Configuration Store extends the Vert.x Configuration Retriever and provides the
 * way to retrieve configuration from a Zookeeper server. It uses Apache Curator as client.
 *
 * ==== Using the Zookeeper Configuration Store
 *
 * To use the Redis Configuration Store, add the following dependency to the
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
 * <dependency>
 *   <groupId>${maven.groupId}</groupId>
 *   <artifactId>vertx-config</artifactId>
 *   <version>${maven.version}</version>
 * </dependency>
 * ----
 *
 * * Gradle (in your `build.gradle` file):
 *
 * [source,groovy,subs="+attributes"]
 * ----
 * compile '${maven.groupId}:vertx-config:${maven.version}'
 * compile '${maven.groupId}:${maven.artifactId}:${maven.version}'
 * ----
 *
 * ==== Configuring the store
 *
 * Once added to your classpath or dependencies, you need to configure the
 * {@link io.vertx.config.ConfigRetriever} to use this store:
 *
 * [source, $lang]
 * ----
 * {@link examples.Examples#example1(io.vertx.core.Vertx)}
 * ----
 *
 * The store configuration is used to configure the Apache Curator client and the _path_ of the Zookeeper node
 * containing the configuration. Notice that the format of the configuration can be JSON, or any supported format.
 *
 * The configuration requires the `configuration` attribute indicating the connection _string_ of the Zookeeper
 * server, and the `path` attribute indicating the path of the node containing the configuration.
 *
 * In addition you can configure:
 *
 * * `maxRetries`: the number of connection attempt, 3 by default
 * * `baseSleepTimeBetweenRetries`: the amount of milliseconds to wait between retries (exponential backoff strategy).
 * 1000 ms by default.
 *
 */
@Document(fileName = "zookeeper-store.adoc")
@ModuleGen(name = "vertx-config", groupPackage = "io.vertx")
package io.vertx.config.zookeeper;

import io.vertx.codegen.annotations.ModuleGen;
import io.vertx.docgen.Document;

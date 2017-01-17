/**
 * = Redis Configuration Store
 *
 * The Redis Configuration Store extends the Vert.x Configuration Retriever and provides the
 * way to retrieve configuration from a Redis server.
 *
 * == Using the Redis Configuration Store
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
 * == Configuring the store
 *
 * Once added to your classpath or dependencies, you need to configure the
 * {@link io.vertx.config.ConfigurationRetriever} to use this store:
 *
 * [source, $lang]
 * ----
 * {@link examples.Examples#example1(io.vertx.core.Vertx)}
 * ----
 *
 * The store configuration is used to create an instance of
 * {@link io.vertx.redis.RedisClient}. check the documentation of the Vert.x Redis Client
 * for further details.
 *
 * In addition, you can set the `key` instructing the store in which _field_ the configuration
 * is stored. `configuration` is used by default.
 *
 * The created Redis client retrieves the configuration using the `HGETALL` configuration.
 *
 */
@Document(fileName = "index.adoc")
@ModuleGen(name = "vertx-config", groupPackage = "io.vertx")
package io.vertx.config.redis;

import io.vertx.codegen.annotations.ModuleGen;
import io.vertx.docgen.Document;
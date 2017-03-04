/**
 * === Spring Config Server Store
 *
 * The Spring Config Server Store extends the Vert.x Configuration Retriever and provides the
 * a way to retrieve configuration from a Spring Server.
 *
 * ==== Using the Spring Config Server Store
 *
 * To use the Spring Config Server Store, add the following dependency to the
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
 *
 * Configurable attributes are:
 *
 * * `url` - the `url` to retrieve the configuration (mandatory)
 * * `timeout` - the timeout (in milliseconds) to retrieve the configuration, 3000 by default
 * * `user` - the `user` (no authentication by default)
 * * `password` - the `password`
 * * `httpClientConfiguration` - the configuration of the underlying HTTP client
 *
 */
@Document(fileName = "spring-store.adoc")
@ModuleGen(name = "vertx-config", groupPackage = "io.vertx")
package io.vertx.config.spring;

import io.vertx.codegen.annotations.ModuleGen;
import io.vertx.docgen.Document;

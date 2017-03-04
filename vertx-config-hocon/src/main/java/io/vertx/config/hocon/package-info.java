/**
 * === Hocon Configuration Format
 *
 * The Hocon Configuration Format extends the Vert.x Configuration Retriever and provides the
 * support for the HOCON(https://github.com/typesafehub/config/blob/master/HOCON.md) format.
 *
 * It supports includes, json, properties, macros...
 *
 * ==== Using the Hocon Configuration Format
 *
 * To use the Hocon Configuration Format, add the following dependency to the
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
 * ==== Configuring the store to use HOCON
 *
 * Once added to your classpath or dependencies, you need to configure the
 * {@link io.vertx.config.ConfigRetriever} to use this format:
 *
 * [source, $lang]
 * ----
 * {@link examples.Examples#example1(io.vertx.core.Vertx)}
 * ----
 *
 * You just need to set `format` to `hocon`.
 */
@Document(fileName = "hocon-format.adoc")
@ModuleGen(name = "vertx-config", groupPackage = "io.vertx")
package io.vertx.config.hocon;

import io.vertx.codegen.annotations.ModuleGen;
import io.vertx.docgen.Document;

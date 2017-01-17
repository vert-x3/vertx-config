/**
 * = Yaml Configuration Format
 *
 * The Yaml Configuration Format extends the Vert.x Configuration Retriever and provides the
 * support for the Yaml Configuration Format format.
 *
 * == Using the Yaml Configuration Format
 *
 * To use the Yaml Configuration Format, add the following dependency to the
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
 * == Configuring the store to use YAML
 *
 * Once added to your classpath or dependencies, you need to configure the
 * {@link io.vertx.config.ConfigurationRetriever} to use this format:
 *
 * [source, $lang]
 * ----
 * {@link examples.Examples#example1(io.vertx.core.Vertx)}
 * ----
 *
 * You just need to set `format` to `yaml`.
 */
@Document(fileName = "index.adoc")
@ModuleGen(name = "vertx-config", groupPackage = "io.vertx")
package io.vertx.config.yaml;

import io.vertx.codegen.annotations.ModuleGen;
import io.vertx.docgen.Document;
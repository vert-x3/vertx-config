/**
 * = Git Configuration Store
 *
 * The Git Configuration Store is an extension to the Vert.x Configuration Retriever to
 * retrieve configuration from a Git repository.
 *
 * == Using the Git Configuration Store
 *
 * To use the Git Configuration, add the following dependency to the
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
 * {@link io.vertx.config.ConfigRetriever} to use this store:
 *
 * [source, $lang]
 * ----
 * {@link examples.Examples#example1(io.vertx.core.Vertx)}
 * ----
 *
 * The configuration requires:
 *
 * * the `url` of the repository
 * * the `path` where the repository is cloned (local directory)
 * * at least `fileset` indicating the set of files to read (same behavior as the
 * directory configuration store).
 *
 * You can also configure the `branch` (`master` by default) to use and the name of the
 * `remote` repository (`origin` by default).
 *
 * == How does it works
 *
 * If the local `path` does not exist, the configuration store clones the repository into
 * this directory. Then it reads the file matching the different file sets.
 *
 * It the local `path` exist, it tried to update it (it switches branch if needed)). If the
 * update failed the configuration retrieval fails.
 *
 * Periodically, the repositories is updated to check if the configuration has been updated.
 *
 */
@Document(fileName = "index.adoc")
@ModuleGen(name = "vertx-config", groupPackage = "io.vertx")
package io.vertx.config.git;

import io.vertx.codegen.annotations.ModuleGen;
import io.vertx.docgen.Document;
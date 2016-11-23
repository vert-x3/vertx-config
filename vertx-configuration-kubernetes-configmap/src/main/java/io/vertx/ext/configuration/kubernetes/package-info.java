/**
 * = Kubernetes ConfigMap Store
 *
 * The Kubernetes ConfigMap Store extends the Vert.x Configuration Retriever and provides the
 * support Kubernetes Config Map and Secrets. So, configuration is retrieved by reading the config map or the secrets.
 *
 * == Using the Kubernetes ConfigMap Store
 *
 * To use the Kubernetes ConfigMap Store, add the following dependency to the
 * _dependencies_ section of your build descriptor:
 *
 * * Maven (in your `pom.xml`):
 *
 * [source,xml,subs="+attributes"]
 * ----
 * <dependency>
 * <groupId>${maven.groupId}</groupId>
 * <artifactId>${maven.artifactId}</artifactId>
 * <version>${maven.version}</version>
 * </dependency>
 * <dependency>
 * <groupId>${maven.groupId}</groupId>
 * <artifactId>vertx-configuration</artifactId>
 * <version>${maven.version}</version>
 * </dependency>
 * ----
 *
 * * Gradle (in your `build.gradle` file):
 *
 * [source,groovy,subs="+attributes"]
 * ----
 * compile '${maven.groupId}:vertx-configuration:${maven.version}'
 * compile '${maven.groupId}:${maven.artifactId}:${maven.version}'
 * ----
 *
 * == Configuring the store
 *
 * Once added to your classpath or dependencies, you need to configure the
 * {@link io.vertx.ext.configuration.ConfigurationRetriever} to use this store:
 *
 * [source, $lang]
 * ----
 * {@link examples.Examples#example1(io.vertx.core.Vertx)}
 * ----
 *
 * You need to configure the store to find the right configmap. this is done with:
 *
 * * `namespace` - the project namespace, `default` by default. If the `KUBERNETES_NAMESPACE` ENV variable is set, it
 * uses this value.
 * * `name` - the name of the config map
 *
 * If the config map is composed by several element, you can use the `key` parameter to tell
 * which `key` is read
 *
 * The application must have the permissions to read the config map.
 *
 * To read data from a secret, just configure the `secret` property to `true`:
 *
 * [source, $lang]
 * ----
 * {@link examples.Examples#example2(io.vertx.core.Vertx)}
 * ----
 */
@Document(fileName = "index.adoc")
@ModuleGen(name = "vertx-configuration", groupPackage = "io.vertx")
package io.vertx.ext.configuration.kubernetes;

import io.vertx.codegen.annotations.ModuleGen;
import io.vertx.docgen.Document;
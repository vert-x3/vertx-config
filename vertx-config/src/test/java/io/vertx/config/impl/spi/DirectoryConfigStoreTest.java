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

package io.vertx.config.impl.spi;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Check the behavior of the {@link DirectoryConfigStore}
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class DirectoryConfigStoreTest extends ConfigStoreTestBase {

  @Before
  public void init() {
    factory = new DirectoryConfigStoreFactory();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWithMissingPathInConf() {
    store = factory.create(vertx, new JsonObject().put("no-path", ""));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWithMissingFileSets() {
    store = factory.create(vertx, new JsonObject().put("path",
        "src/test/resources"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWithMissingPatternInAFileSet() {
    store = factory.create(vertx, new JsonObject().put("path", "src/test/resources")
        .put("filesets", new JsonArray().add(new JsonObject().put("format", "properties"))));
  }

  @Test
  public void testName() {
    assertThat(factory.name()).isNotNull().isEqualTo("directory");
  }

  @Test
  public void testWithNonExistingPath(TestContext tc) {
    Async async = tc.async();
    store = factory.create(vertx, new JsonObject().put("path", "src/test/missing")
        .put("filesets", new JsonArray().add(new JsonObject().put("pattern", "*.json"))));
    store.get(ar -> {
      assertThat(ar.succeeded()).isTrue();
      assertThat(ar.result()).isEqualTo(Buffer.buffer("{}"));
      async.complete();
    });
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWithAPathThatIsAFile() {
    store = factory.create(vertx, new JsonObject().put("path", "src/test/resources/file/regular.json")
        .put("filesets", new JsonArray().add(new JsonObject().put("pattern", "*.json"))));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testWhenTheFormatIsUnknown() {
    store = factory.create(vertx, new JsonObject().put("path", "src/test/resources/file")
        .put("filesets", new JsonArray().add(new JsonObject().put("pattern", "*.json").put("format", "bad"))));
  }

  @Test
  public void testLoadingASingleJsonFile(TestContext context) {
    Async async = context.async();
    retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions()
        .addStore(new ConfigStoreOptions()
            .setType("directory")
            .setConfig(new JsonObject().put("path", "src/test/resources")
                .put("filesets", new JsonArray().add(new JsonObject().put("pattern", "file/reg*.json"))))));
    retriever.getConfig(ar -> {
      ConfigChecker.check(ar);
      async.complete();
    });
  }

  @Test
  public void testLoadingASinglePropertiesFile(TestContext context) {
    Async async = context.async();
    retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions()
        .addStore(new ConfigStoreOptions()
            .setType("directory")
            .setConfig(new JsonObject().put("path", "src/test/resources")
                .put("filesets", new JsonArray().add(new JsonObject()
                    .put("format", "properties")
                    .put("pattern", "**/reg*.properties"))))));

    retriever.getConfig(ar -> {
      assertThat(ar.succeeded()).isTrue();
      JsonObject json = ar.result();
      assertThat(json.getString("key")).isEqualTo("value");
      assertThat(json.getBoolean("true")).isTrue();
      assertThat(json.getBoolean("false")).isFalse();
      assertThat(json.getString("missing")).isNull();
      assertThat(json.getInteger("int")).isEqualTo(5);
      assertThat(json.getDouble("float")).isEqualTo(25.3);
      async.complete();
    });
  }

  @Test
  public void testLoadingASinglePropertiesFileWithRawData(TestContext context) {
    Async async = context.async();
    retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions()
      .addStore(new ConfigStoreOptions()
        .setType("directory")
        .setConfig(new JsonObject().put("path", "src/test/resources")
          .put("filesets", new JsonArray().add(new JsonObject()
            .put("format", "properties")
            .put("pattern", "**/raw.properties")
            .put("raw-data", true))))));

    retriever.getConfig(ar -> {
      assertThat(ar.succeeded()).isTrue();
      JsonObject json = ar.result();
      assertThat(json.getString("key")).isEqualTo("value");
      assertThat(json.getString("name")).isEqualTo("123456789012345678901234567890123456789012345678901234567890");
      assertThat(json.getString("true")).isEqualTo("true");
      assertThat(json.getString("false")).isEqualTo("false");
      assertThat(json.getString("missing")).isNull();
      assertThat(json.getString("int")).isEqualTo("5");
      assertThat(json.getString("float")).isEqualTo("25.3");
      async.complete();
    });
  }


  @Test
  public void testWhenNoFileMatch(TestContext context) {
    Async async = context.async();
    retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions()
        .addStore(new ConfigStoreOptions()
            .setType("directory")
            .setConfig(new JsonObject().put("path", "src/test/resources")
                .put("filesets", new JsonArray().add(new JsonObject()
                    .put("format", "properties")
                    .put("pattern", "**/reg*.stuff"))))));

    retriever.getConfig(ar -> {
      assertThat(ar.succeeded()).isTrue();
      JsonObject json = ar.result();
      assertThat(json.isEmpty());
      async.complete();
    });
  }

  @Test
  public void testWith2FileSetsAndNoIntersection(TestContext context) {
    Async async = context.async();
    retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions()
        .addStore(new ConfigStoreOptions()
            .setType("directory")
            .setConfig(new JsonObject().put("path", "src/test/resources")
                .put("filesets", new JsonArray()
                    .add(new JsonObject().put("pattern", "file/reg*.json"))
                    .add(new JsonObject().put("pattern", "dir/a.*son"))
                ))));
    retriever.getConfig(ar -> {
      ConfigChecker.check(ar);
      assertThat(ar.result().getString("a.name")).isEqualTo("A");
      async.complete();
    });
  }

  @Test
  public void testWith2FileSetsAndWithIntersection(TestContext context) {
    Async async = context.async();
    retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions()
        .addStore(new ConfigStoreOptions()
            .setType("directory")
            .setConfig(new JsonObject().put("path", "src/test/resources")
                .put("filesets", new JsonArray()
                    .add(new JsonObject().put("pattern", "dir/b.json"))
                    .add(new JsonObject().put("pattern", "dir/a.*son"))
                ))));
    retriever.getConfig(ar -> {
      assertThat(ar.result().getString("a.name")).isEqualTo("A");
      assertThat(ar.result().getString("b.name")).isEqualTo("B");
      assertThat(ar.result().getString("conflict")).isEqualTo("A");
      async.complete();
    });
  }

  @Test
  public void testWith2FileSetsAndWithIntersectionReversed(TestContext context) {
    Async async = context.async();
    retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions()
        .addStore(new ConfigStoreOptions()
            .setType("directory")
            .setConfig(new JsonObject().put("path", "src/test/resources")
                .put("filesets", new JsonArray()
                    .add(new JsonObject().put("pattern", "dir/a.*son"))
                    .add(new JsonObject().put("pattern", "dir/b.json"))
                ))));
    retriever.getConfig(ar -> {
      assertThat(ar.result().getString("a.name")).isEqualTo("A");
      assertThat(ar.result().getString("b.name")).isEqualTo("B");
      assertThat(ar.result().getString("conflict")).isEqualTo("B");
      async.complete();
    });
  }

  @Test
  public void testWithAFileSetMatching2FilesWithConflict(TestContext context) {
    Async async = context.async();
    retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions()
        .addStore(new ConfigStoreOptions()
            .setType("directory")
            .setConfig(new JsonObject().put("path", "src/test/resources")
                .put("filesets", new JsonArray()
                    .add(new JsonObject().put("pattern", "dir/?.json"))
                ))));
    retriever.getConfig(ar -> {
      assertThat(ar.result().getString("b.name")).isEqualTo("B");
      assertThat(ar.result().getString("a.name")).isEqualTo("A");
      // Alphabetical order, so B is last.
      assertThat(ar.result().getString("conflict")).isEqualTo("B");
      async.complete();
    });
  }

  @Test
  public void testWithDeepConfigMerge(TestContext context) {
    Async async = context.async();
    retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions()
      .addStore(new ConfigStoreOptions()
        .setType("directory")
        .setConfig(new JsonObject().put("path", "src/test/resources")
          .put("filesets", new JsonArray()
            .add(new JsonObject().put("pattern", "dir/?.json"))
          ))));
    retriever.getConfig(ar -> {
      // Both level-3 objects must exist.
      assertThat(ar.result().getJsonObject("parent").getJsonObject("level_2").getString("key1")).isEqualTo("A");
      assertThat(ar.result().getJsonObject("parent").getJsonObject("level_2").getString("key2")).isEqualTo("B");
      async.complete();
    });
  }

  @Test
  public void testWithAFileSetMatching2FilesWithoutConflict(TestContext context) {
    Async async = context.async();
    retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions()
        .addStore(new ConfigStoreOptions()
            .setType("directory")
            .setConfig(new JsonObject().put("path", "src/test/resources")
                .put("filesets", new JsonArray()
                    .add(new JsonObject().put("pattern", "dir/a?.json"))
                ))));
    retriever.getConfig(ar -> {
      assertThat(ar.result().getString("b.name")).isNull();
      assertThat(ar.result().getString("a.name")).isEqualTo("A");
      assertThat(ar.result().getString("c.name")).isEqualTo("C");
      assertThat(ar.result().getString("conflict")).isEqualTo("A");
      async.complete();
    });
  }

  @Test
  public void testWithAFileSetMatching2FilesOneNotBeingAJsonFile(TestContext context) {
    Async async = context.async();
    retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions()
        .addStore(new ConfigStoreOptions()
            .setType("directory")
            .setConfig(new JsonObject().put("path", "src/test/resources")
                .put("filesets", new JsonArray()
                    .add(new JsonObject().put("pattern", "dir/a?*.json"))
                ))));
    retriever.getConfig(ar -> {
      assertThat(ar.failed());
      assertThat(ar.cause()).isInstanceOf(DecodeException.class);
      async.complete();
    });
  }


}

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
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;

/**
 * This test checks the behavior when using the "raw-data" attribute.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class EnvVariablesConfigStoreWithMockEnvTest extends ConfigStoreTestBase {

  @Test
  public void testLoadingFromEnvUsingRawData() {
    retriever = ConfigRetriever.create(vertx,
      new ConfigRetrieverOptions()
        .addStore(new ConfigStoreOptions().setType("mock-env").setConfig(new JsonObject()
            .put("raw-data", true)
            .put("env", Collections.singletonMap("mock.name", "12345678901234567890"))
          )
        )
    );

    AtomicBoolean done = new AtomicBoolean();

    retriever.getConfig().onComplete(ar -> {
      assertThat(ar.succeeded()).isTrue();
      assertThat(ar.result().getString("PATH")).isNotNull();
      assertThat(ar.result().getString("mock.name")).isEqualTo("12345678901234567890");
      done.set(true);
    });
    await().untilAtomic(done, is(true));
  }

  @Test
  public void testLoadingFromEnvWithoutRawData() {
    retriever = ConfigRetriever.create(vertx,
      new ConfigRetrieverOptions()
        .addStore(new ConfigStoreOptions().setType("mock-env").setConfig(new JsonObject()
            .put("env", Collections.singletonMap("mock.name", "12345678901234567890"))
          )
        )
    );

    AtomicBoolean done = new AtomicBoolean();

    retriever.getConfig().onComplete(ar -> {
      assertThat(ar.succeeded()).isTrue();
      try {
        // We don't mind the value (truncated, we just want to make sure it doesn't throw an exception)
        assertThat(ar.result().getInteger("mock.name")).isNotNull();
      } catch (ClassCastException e) {
        throw new AssertionError("Should not throw exception", e);
      }
      done.set(true);
    });
    await().untilAtomic(done, is(true));
  }

  @Test
  public void testHierarchicalLoadingFromEnv() {
    retriever = ConfigRetriever.create(vertx,
      new ConfigRetrieverOptions()
        .addStore(new ConfigStoreOptions().setType("mock-env").setConfig(new JsonObject()
            .put("hierarchical", true)
            .put("env", Collections.singletonMap("mock.name", "12345678901234567890"))
          )
        )
    );

    AtomicBoolean done = new AtomicBoolean();

    retriever.getConfig().onComplete(ar -> {
      assertThat(ar.succeeded()).isTrue();
      try {
        // We don't mind the value (truncated, we just want to make sure it doesn't throw an exception)
        assertThat(ar.result().getJsonObject("mock")).isNotNull();
        assertThat(ar.result().getJsonObject("mock").getInteger("name")).isNotNull();
      } catch (ClassCastException e) {
        throw new AssertionError("Should not throw exception", e);
      }
      done.set(true);
    });
    await().untilAtomic(done, is(true));
  }
}

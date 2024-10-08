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

package io.vertx.config.tests.spi;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * This test checks the behavior when using the "raw-data" attribute.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class SystemPropertiesConfigStoreWithRawDataTest extends ConfigStoreTestBase {

//  @Rule
//  public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

  @Test
  public void testLoadingFromSysUsingRawData() throws Exception {
    retriever = ConfigRetriever.create(vertx,
      new ConfigRetrieverOptions()
        .addStore(new ConfigStoreOptions().setType("sys").setConfig(new JsonObject().put("raw-data", true)))
    );

    AtomicBoolean done = new AtomicBoolean();

    System.setProperty("name", "12345678901234567890");

    JsonObject res = retriever.getConfig().await(20, TimeUnit.SECONDS);
    assertEquals("12345678901234567890", res.getString("name"));
  }

  @Test
  public void testLoadingFromSysWithoutRawData() throws Exception {
    retriever = ConfigRetriever.create(vertx,
      new ConfigRetrieverOptions()
        .addStore(new ConfigStoreOptions().setType("sys"))
    );

    System.setProperty("name", "12345678901234567891");


    JsonObject res = retriever.getConfig().await(20, TimeUnit.SECONDS);
    try {
      // We don't mind the value (truncated, we just want to make sure it doesn't throw an exception)
      assertThat(res.getInteger("name")).isNotNull();
    } catch (ClassCastException e) {
      throw new AssertionError("Should not throw exception", e);
    }
  }
}

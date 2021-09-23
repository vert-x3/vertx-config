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

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(VertxUnitRunner.class)
public class RawDataTrueEnvVariablesConfigStoreTest extends ConfigStoreTestBase {
  private static final Map<String, String> ENV = new HashMap<>();
  private static final String KEY_1 = "SOME_NUMBER";
  private static final String VAL_1 = "1234567890";
  private static final String KEY_2 = "SOME_BOOLEAN";
  private static final String VAL_2 = "true";

  @Before
  public void init() {
    Map<String, String> env = new HashMap<>();
    env.put(KEY_1, VAL_1);
    env.put(KEY_2, VAL_2);
    factory = new EnvVariablesConfigStoreFactory(() -> env);
    store = factory.create(vertx, new JsonObject().put("raw-data", true));
  }

  @Test
  public void testName() {
    assertThat(factory.name()).isNotNull().isEqualTo("env");
  }

  @Test
  public void testLoadingFromEnvironmentVariables(TestContext context) {
    Async async = context.async();
    getJsonConfiguration(vertx, store, ar -> {
      assertThat(ar.succeeded()).isTrue();
      assertThat(ar.result().getString(KEY_1)).isEqualTo(VAL_1);
      assertThat(ar.result().getString(KEY_2)).isEqualTo(VAL_2);
      async.complete();
    });
  }
}

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

import io.vertx.core.AsyncResult;
import io.vertx.core.json.JsonObject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ConfigChecker {

  public static void check(JsonObject json) {
    assertThat(json).isNotNull();
    assertThat(json.getString("key")).isEqualTo("value");

    assertThat(json.getBoolean("true")).isTrue();
    assertThat(json.getBoolean("false")).isFalse();

    assertThat(json.getString("missing")).isNull();

    assertThat(json.getInteger("int")).isEqualTo(5);
    assertThat(json.getDouble("float")).isEqualTo(25.3);

    assertThat(json.getJsonArray("array").size()).isEqualTo(3);
    assertThat(json.getJsonArray("array").contains(1)).isTrue();
    assertThat(json.getJsonArray("array").contains(2)).isTrue();
    assertThat(json.getJsonArray("array").contains(3)).isTrue();

    assertThat(json.getJsonObject("sub").getString("foo")).isEqualTo("bar");
  }

  public static void check(AsyncResult<JsonObject> ar) {
    if (ar.failed()) {
      ar.cause().printStackTrace();
      fail("Success expected", ar.cause());
    } else {
      check(ar.result());
    }
  }

}

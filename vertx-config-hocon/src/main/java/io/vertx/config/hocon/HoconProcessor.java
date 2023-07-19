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

package io.vertx.config.hocon;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import io.vertx.config.spi.ConfigProcessor;
import io.vertx.config.spi.utils.JsonObjectHelper;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import java.io.Reader;
import java.io.StringReader;

/**
 * A processor using Typesafe Conf to read Hocon files. It also support JSON and Properties.
 * More details on Hocon and the used library on the
 * <a href="https://github.com/typesafehub/config">Hocon documentation page</a>.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class HoconProcessor implements ConfigProcessor {
  private static final String ENV_OVERRIDE_KEY = "hocon.env.override";
  private static final String RAW_DATA_KEY = "raw-data";
  @Override
  public String name() {
    return "hocon";
  }

  @Override
  public Future<JsonObject> process(Vertx vertx, JsonObject configuration, Buffer input) {
    // Use executeBlocking even if the bytes are in memory
    // Indeed, HOCON resolution can read others files (includes).
    return vertx.executeBlocking(() -> {
        try (Reader reader = new StringReader(input.toString("UTF-8"))) {
          Config conf = ConfigFactory.parseReader(reader);
          conf = conf.resolve();
          String output = conf.root().render(ConfigRenderOptions.concise()
            .setJson(true).setComments(false).setFormatted(false));
          JsonObject json = new JsonObject(output);
          if (configuration != null && configuration.getBoolean(ENV_OVERRIDE_KEY, false)) {
            final JsonObject envOverrideJson = new JsonObject();
            ConfigFactory.systemEnvironmentOverrides().entrySet()
              .forEach(e -> JsonObjectHelper.put(envOverrideJson, e.getKey(), e.getValue().unwrapped().toString(),
                configuration.getBoolean(RAW_DATA_KEY, false)));
            if (!envOverrideJson.isEmpty()) {
              json = json.mergeIn(envOverrideJson);
            }
          }
          return json;
        }
      }
    );
  }
}

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
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

/**
 * A processor using Typesafe Conf to read Hocon files. It also support JSON and Properties.
 * More details on Hocon and the used library on the
 * <a href="https://github.com/typesafehub/config">Hocon documentation page</a>.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class HoconProcessor implements ConfigProcessor {
  @Override
  public String name() {
    return "hocon";
  }

  @Override
  public void process(Vertx vertx, JsonObject configuration, Buffer input, Handler<AsyncResult<JsonObject>> handler) {
    // Use executeBlocking even if the bytes are in memory
    // Indeed, HOCON resolution can read others files (includes).
    vertx.executeBlocking(
        future -> {
          try (Reader reader = new StringReader(input.toString(StandardCharsets.UTF_8))){
            Config conf = ConfigFactory.parseReader(reader);
            conf = conf.resolve();
            String output = conf.root().render(ConfigRenderOptions.concise()
                .setJson(true).setComments(false).setFormatted(false));
            JsonObject json = new JsonObject(output);
            future.complete(json);
          } catch (Exception e) {
            future.fail(e);
          }
        },
        handler
    );
  }
}

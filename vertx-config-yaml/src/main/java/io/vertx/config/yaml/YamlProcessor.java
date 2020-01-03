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

package io.vertx.config.yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.vertx.config.spi.ConfigProcessor;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.json.JsonObject;

/**
 * A processor using Jackson and SnakeYaml to read Yaml files.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class YamlProcessor implements ConfigProcessor {

  public static ObjectMapper YAML_MAPPER = new YAMLMapper();

  @Override
  public String name() {
    return "yaml";
  }

  @Override
  public Future<JsonObject> process(Vertx vertx, JsonObject configuration, Buffer input) {
    if (input.length() == 0) {
      // the parser does not support empty files, which should be managed to be homogeneous
      return ((ContextInternal) vertx.getOrCreateContext()).succeededFuture(new JsonObject());
    }

    // Use executeBlocking even if the bytes are in memory
    return vertx.executeBlocking(promise -> {
      try {
        JsonNode root = YAML_MAPPER.readTree(input.toString("utf-8"));
        JsonObject json = new JsonObject(root.toString());
        promise.complete(json);
      } catch (Exception e) {
        promise.fail(e);
      }
    });
  }
}

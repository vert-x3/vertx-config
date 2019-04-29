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

import io.vertx.config.spi.ConfigProcessor;
import io.vertx.config.spi.utils.JsonObjectHelper;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * Transforms properties to json.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class PropertiesConfigProcessor implements ConfigProcessor {

  private final static PropertiesReader FLAT_READER = new FlatPropertiesReader();

  private final static PropertiesReader HIERARCHICAL_READER = new HierarchicalPropertiesReader();

  @Override
  public String name() {
    return "properties";
  }

  @Override
  public void process(Vertx vertx, JsonObject configuration, Buffer input, Handler<AsyncResult<JsonObject>> handler) {
    Boolean hierarchicalData = configuration.getBoolean("hierarchical", false);
    PropertiesReader reader = hierarchicalData ? HIERARCHICAL_READER : FLAT_READER;
    // I'm not sure the executeBlocking is really required here as the
    // buffer is in memory,
    // so the input stream is not blocking
    vertx.executeBlocking(future -> {
      byte[] bytes = input.getBytes();
      ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
      try {
        JsonObject created = reader.readAsJson(configuration, stream);
        future.complete(created);
      } catch (Exception e) {
        future.fail(e);
      } finally {
        closeQuietly(stream);
      }
    }, handler);
  }

  public static void closeQuietly(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (Exception e) {
        // Ignore it.
      }
    }
  }

  private interface PropertiesReader {

    JsonObject readAsJson(JsonObject configuration, InputStream stream) throws IOException;
  }

  private static class FlatPropertiesReader implements PropertiesReader {

    @Override
    public JsonObject readAsJson(JsonObject configuration, InputStream byteStream) throws IOException {
      Properties properties = new Properties();
      properties.load(byteStream);
      return JsonObjectHelper.from(properties, configuration.getBoolean("raw-data", false));
    }
  }

  private static class HierarchicalPropertiesReader implements PropertiesReader {

    @Override
    public JsonObject readAsJson(JsonObject configuration, InputStream byteStream) throws IOException {
      try (
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(byteStream));
        Stream<String> stream = bufferedReader.lines()
      ) {
        return toJson(stream);
      }
    }

    private JsonObject toJson(Stream<String> stream) {
      return stream
        .filter( line -> {
        	line.trim();
        	return  !line.isEmpty()
        			&& !line.startsWith("#")
        			&& !line.startsWith("!");
          })
        .map(line -> line.split("="))
        .map(raw -> {
          String property = raw[0].trim();
          Object value = tryParse(raw[1].trim());
          List<String> paths = asList(property.split("\\."));
          if (paths.size() == 1) {
            return new JsonObject().put(property, value);
          }
          JsonObject json = toJson(paths.subList(1, paths.size()), value);
          return new JsonObject().put(paths.get(0), json);
        })
        .reduce((json, other) -> json.mergeIn(other, true))
        .orElse(new JsonObject());

    }

    private JsonObject toJson(List<String> paths, Object value) {
      if (paths.size() == 0) return new JsonObject();
      if (paths.size() == 1) {
        return new JsonObject().put(paths.get(0), value);
      }
      String path = paths.get(0);
      JsonObject jsonValue = toJson(paths.subList(1, paths.size()), value);
      return new JsonObject().put(path, jsonValue);
    }

    private Object tryParse(String raw) {
      if (raw.contains(",")) {
        return Stream.of(raw.split(","))
          .map(this::tryParse)
          .collect(collectingAndThen(toList(), JsonArray::new));
      }
      if ("true".equals(raw)) {
        return true;
      }
      if ("false".equals(raw)) {
        return false;
      }
      if (raw.matches("^\\d+\\.\\d+$")) {
        return Double.parseDouble(raw);
      }
      if (raw.matches("^\\d+$")) {
        return Integer.parseInt(raw);
      }
      return raw;
    }
  }
}

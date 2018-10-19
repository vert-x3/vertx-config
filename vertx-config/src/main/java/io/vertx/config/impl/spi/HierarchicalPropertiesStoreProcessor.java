package io.vertx.config.impl.spi;

import io.vertx.config.spi.ConfigProcessor;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;


/**
 * Transforms properties to true json:
 * <code>
 * <p>
 * server.port = 8080
 * <p>
 * server.host=http://localhost
 * <p>
 * multivalue=1,2,3
 * </code>
 * <p>to<p>
 *
 * <code>
 *     {
 *         "server" : {
 *             "port" : 8080,
 *             "host" : "http://localhost"
 *  *         },
 *         "multivalue" : [ 1, 2, 3]
 *     }
 * </code>
 * <p> Standard implementation create to:
 * <code>
 *     {
 *         "server.port" : 8080,
 *         "server.host" : "http://localhost",
 *         "multivalue" : "1,2,3"
 *     }
 * </code>
 *
 * @author Eugene Utkin (evgeny.utkin@mediascope.net)
 */
public class HierarchicalPropertiesStoreProcessor implements ConfigProcessor {

  @Override
  public String name() {
    return "hierarchical-properties";
  }

  @Override
  public void process(Vertx vertx, JsonObject configuration, Buffer input, Handler<AsyncResult<JsonObject>> handler) {
    vertx.executeBlocking(future -> {
      try (
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.getBytes());
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        Stream<String> stream = bufferedReader.lines()
      ) {
        future.complete(toJson(stream));
      } catch (IOException e) {
        future.fail(e);
      }
    }, handler);
  }

  private JsonObject toJson(Stream<String> stream) {
    return stream
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

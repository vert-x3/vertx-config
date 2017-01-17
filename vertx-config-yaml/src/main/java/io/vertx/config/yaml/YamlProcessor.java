package io.vertx.config.yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.vertx.config.spi.ConfigurationProcessor;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

/**
 * A processor using Jackson and SnakeYaml to read Yaml files.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class YamlProcessor implements ConfigurationProcessor {

  public static ObjectMapper YAML_MAPPER = new YAMLMapper();

  @Override
  public String name() {
    return "yaml";
  }

  @Override
  public void process(Vertx vertx, JsonObject configuration, Buffer input, Handler<AsyncResult<JsonObject>> handler) {
    if (input.length() == 0) {
      // the parser does not support empty files, which should be managed to be homogeneous
      handler.handle(Future.succeededFuture(new JsonObject()));
      return;
    }

    // Use executeBlocking even if the bytes are in memory
    vertx.executeBlocking(
        future -> {
          try {
            JsonNode root = YAML_MAPPER.readTree(input.toString("utf-8"));
            JsonObject json = new JsonObject(root.toString());
            future.complete(json);
          } catch (Exception e) {
            future.fail(e);
          }
        },
        handler
    );
  }
}

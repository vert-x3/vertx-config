package io.vertx.config.toml;

import com.moandjiezana.toml.Toml;
import io.vertx.config.spi.ConfigProcessor;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * A processor for the <a href='https://github.com/toml-lang/toml'>TOML</a>
 * format.
 * </p>
 * <p>
 * Currently using the
 * <a href='https://github.com/mwanji/toml4j'>toml4j library</a>. If a newer
 * version of this library becomes available, you can override the dependency
 * (assuming the API does not change).
 * </p>
 *
 * @author <a href='lyndon.codes'>Lyndon Armitage</a>
 */
public final class TomlProcessor implements ConfigProcessor {

  @Override
  public final String name() {
    return "toml";
  }

  @Override
  public final void process(
    Vertx vertx,
    JsonObject configuration,
    Buffer input,
    Handler<AsyncResult<JsonObject>> handler
  ) {
    Objects.requireNonNull(vertx, "vertx cannot be null");
    Objects.requireNonNull(input, "input cannot be null");
    Objects.requireNonNull(handler, "handler cannot be null");

    if (input.length() == 0) {
      handler.handle(Future.succeededFuture(new JsonObject()));
      return;
    }

    vertx.executeBlocking(
      future -> {
        // Currently loads the whole config file into memory
        try (
          ByteArrayInputStream inputStream = new ByteArrayInputStream(
            input.getBytes()
          )
        ) {

          Toml toml = new Toml().read(inputStream);
          Map<String, Object> asMap = Objects.requireNonNull(
            toml.toMap(),
            "toml library returned a null map"
          );
          JsonObject asJson = new JsonObject(asMap);
          future.complete(asJson);
        } catch (Exception e) {
          future.fail(e);
        }
      },
      handler
    );
  }
}

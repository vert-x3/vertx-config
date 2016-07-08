package io.vertx.ext.configuration.impl.spi;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.configuration.spi.ConfigurationProcessor;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.Properties;

/**
 * Transforms properties to json.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class PropertiesConfigurationProcessor implements ConfigurationProcessor {

  @Override
  public String name() {
    return "properties";
  }

  @Override
  public void process(Vertx vertx, JsonObject configuration, Buffer input, Handler<AsyncResult<JsonObject>> handler) {
    // I'm not sure the executeBlocking is really required here as the buffer is in memory,
    // to the input stream is not blocking
    vertx.executeBlocking(
        future -> {
          byte[] bytes = input.getBytes();
          ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
          Properties properties = new Properties();
          try {
            properties.load(stream);
            future.complete(JsonObjectHelper.from(properties));
          } catch (Exception e) {
            future.fail(e);
          } finally {
            closeQuietly(stream);
          }
        },
        handler
    );
  }

  public static void closeQuietly(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (IOException e) {
        // Ignore it.
      }
    }
  }
}

package io.vertx.ext.configuration.hocon;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.configuration.spi.ConfigurationProcessor;

import java.io.Reader;
import java.io.StringReader;

import static io.vertx.ext.configuration.impl.spi.PropertiesConfigurationProcessor.closeQuietly;

/**
 * A processor using Typesafe Conf to read Hocon files. It also support JSON and Properties.
 * More details on Hocon and the used library on the
 * <a href="https://github.com/typesafehub/config">Hocon documentation page</a>.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class HoconProcessor implements ConfigurationProcessor {
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
          Reader reader = new StringReader(input.toString("UTF-8"));
          try {
            Config conf = ConfigFactory.parseReader(reader);
            conf = conf.resolve();
            String output = conf.root().render(ConfigRenderOptions.concise()
                .setJson(true).setComments(false).setFormatted(false));
            JsonObject json = new JsonObject(output);
            future.complete(json);
          } catch (Exception e) {
            future.fail(e);
          } finally {
            closeQuietly(reader);
          }
        },
        handler
    );
  }
}

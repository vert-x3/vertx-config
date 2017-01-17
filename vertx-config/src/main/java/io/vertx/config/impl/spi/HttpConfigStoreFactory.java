package io.vertx.config.impl.spi;

import io.vertx.config.spi.ConfigStore;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.config.spi.ConfigStoreFactory;

import java.util.Objects;

/**
 * The factory creating Json File configuration stores.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class HttpConfigStoreFactory implements ConfigStoreFactory {

  @Override
  public String name() {
    return "http";
  }

  @Override
  public ConfigStore create(Vertx vertx, JsonObject configuration) {
    HttpClient client = vertx.createHttpClient(new HttpClientOptions(configuration));
    String host = configuration.getString("host");
    int port = configuration.getInteger("port", 80);
    String path = configuration.getString("path", "/");

    Objects.requireNonNull(host);

    return new HttpConfigStore(host, port, path, client);
  }
}

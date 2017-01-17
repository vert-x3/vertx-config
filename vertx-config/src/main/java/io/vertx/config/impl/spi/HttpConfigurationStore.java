package io.vertx.config.impl.spi;

import io.vertx.config.spi.ConfigurationStore;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;

/**
 * A configuration store retrieving the configuration from a HTTP location
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class HttpConfigurationStore implements ConfigurationStore {

  private final String host;
  private final int port;
  private final String path;
  private final HttpClient client;

  public HttpConfigurationStore(String host, int port, String path, HttpClient client) {
    this.host = host;
    this.port = port;
    this.path = path;
    this.client = client;
  }

  @Override
  public void get(Handler<AsyncResult<Buffer>> completionHandler) {
    client.get(port, host, path, response ->
        response
            .exceptionHandler(t -> completionHandler.handle(Future.failedFuture(t)))
            .bodyHandler(buffer -> completionHandler.handle(Future.succeededFuture(buffer)))
    )
        .exceptionHandler(t -> completionHandler.handle(Future.failedFuture(t)))
        .end();
  }

  @Override
  public void close(Handler<Void> completionHandler) {
    this.client.close();
    completionHandler.handle(null);
  }
}

package examples;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

public class HttpGreetingVerticle extends AbstractVerticle {

  @Override
  public void start() {
    ConfigRetriever retriever = ConfigRetriever.create(vertx);
    retriever.getConfig().onComplete(json -> {
      JsonObject result = json.result();

      vertx.createHttpServer()
        .requestHandler(req -> result.getString("message"))
        .listen(result.getInteger("port"));
    });
  }
}

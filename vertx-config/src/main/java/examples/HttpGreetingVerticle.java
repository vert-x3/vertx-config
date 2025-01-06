package examples;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

public class HttpGreetingVerticle extends AbstractVerticle {

  @Override
  public void start() {
    ConfigRetriever retriever = ConfigRetriever.create(vertx);
    retriever.getConfig().onComplete(result -> {
      JsonObject json = result.result();

      vertx.createHttpServer()
        .requestHandler(req -> req.response().end(json.getString("message")))
        .listen(json.getInteger("port"));
    });
  }
}

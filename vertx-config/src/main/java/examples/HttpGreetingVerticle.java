package examples;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;

public class HttpGreetingVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) {
    ConfigRetriever retriever = ConfigRetriever.create(vertx);
    retriever
      .getConfig()
      .compose(json -> vertx.createHttpServer()
        .requestHandler(req -> req.response().end(json.getString("message")))
        .listen(json.getInteger("port"))
      )
      .<Void>mapEmpty()
      .onComplete(startPromise);
  }
}

package examples;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.Future;
import io.vertx.core.VerticleBase;

public class HttpGreetingVerticle extends VerticleBase {

  @Override
  public Future<?> start() {
    ConfigRetriever retriever = ConfigRetriever.create(vertx);
    return retriever
      .getConfig()
      .compose(json -> vertx.createHttpServer()
        .requestHandler(req -> req.response().end(json.getString("message")))
        .listen(json.getInteger("port"))
      );
  }
}

package io.vertx.config.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;


public class GreetingVerticle extends AbstractVerticle {

  private String message;

  @Override
  public void start(Future<Void> future) {
    message = config().getString("message");
    String address = config().getString("address");

    Future<Void> endpointReady = Future.future();
    Future<Void> updateReady = Future.future();

    vertx.eventBus().<JsonObject>consumer(address + "/update")
      .handler(json -> {
        message = json.body().getString("message");
        json.reply("OK");
      })
      .completionHandler(updateReady);

    vertx.eventBus().consumer(address)
      .handler(msg -> msg.reply(message))
      .completionHandler(endpointReady);

    CompositeFuture.all(endpointReady, updateReady).setHandler(x -> future.handle(x.mapEmpty()));
  }
}

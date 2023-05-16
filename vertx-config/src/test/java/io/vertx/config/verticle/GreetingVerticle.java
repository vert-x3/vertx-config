package io.vertx.config.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;


public class GreetingVerticle extends AbstractVerticle {

  private String message;

  @Override
  public void start(Promise<Void> future) {
    message = config().getString("message");
    String address = config().getString("address");

    Promise<Void> endpointReady = Promise.promise();
    Promise<Void> updateReady = Promise.promise();

    vertx.eventBus().<JsonObject>consumer(address + "/update")
      .handler(json -> {
        message = json.body().getString("message");
        json.reply("OK");
      }).completion().onComplete(updateReady);

    vertx.eventBus().consumer(address)
      .handler(msg -> msg.reply(message))
      .completion().onComplete(endpointReady);

    Future.all(endpointReady.future(), updateReady.future()).onComplete(x -> future.handle(x.mapEmpty()));
  }
}

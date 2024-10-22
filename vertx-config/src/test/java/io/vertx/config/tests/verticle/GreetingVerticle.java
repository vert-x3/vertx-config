package io.vertx.config.tests.verticle;

import io.vertx.core.Future;
import io.vertx.core.VerticleBase;
import io.vertx.core.json.JsonObject;


public class GreetingVerticle extends VerticleBase {

  private String message;

  @Override
  public Future<?> start() throws Exception {
    message = config().getString("message");
    String address = config().getString("address");

    Future<Void> updateReady = vertx.eventBus().<JsonObject>consumer(address + "/update")
      .handler(json -> {
        message = json.body().getString("message");
        json.reply("OK");
      }).completion();

    Future<Void> endpointReady = vertx.eventBus().consumer(address)
      .handler(msg -> msg.reply(message))
      .completion();

    return Future.all(endpointReady, updateReady);
  }
}

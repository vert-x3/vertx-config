package io.vertx.config.impl.spi;

import io.vertx.config.spi.ConfigStore;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;

/**
 * An implementation of configuration store that receive the configuration from the event bus. It
 * listens on a given address and returns the last received configuration to the next enquiry.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class EventBusConfigStore implements ConfigStore {

  private final MessageConsumer<Object> consumer;
  private Buffer last;

  public EventBusConfigStore(Vertx vertx, String address) {
    consumer = vertx.eventBus().consumer(address);
    consumer.handler(message -> {
      Object body = message.body();
      synchronized (EventBusConfigStore.this) {
        if (body instanceof JsonObject) {
          last = Buffer.buffer(((JsonObject) body).encode());
        } else if (body instanceof Buffer) {
          last = (Buffer) body;
        }
      }
    });
  }

  @Override
  public void close(Handler<Void> completionHandler) {
    consumer.unregister(ar -> completionHandler.handle(null));
  }

  @Override
  public void get(Handler<AsyncResult<Buffer>> completionHandler) {
    Buffer buffer;
    synchronized (this) {
      buffer = last;
    }

    if (buffer != null) {
      completionHandler.handle(Future.succeededFuture(buffer));
    } else {
      completionHandler.handle(Future.succeededFuture(Buffer.buffer("{}")));
    }
  }
}

package io.vertx.config.redis;

import io.vertx.config.spi.ConfigurationStore;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;

/**
 * An implementation of configuration store reading hash from Redis.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class RedisConfigurationStore implements ConfigurationStore {

  private final RedisClient redis;
  private final String field;

  public RedisConfigurationStore(Vertx vertx, JsonObject config) {
    this.field = config.getString("key", "configuration");
    this.redis = RedisClient.create(vertx, new RedisOptions(config));
  }

  @Override
  public void close(Handler<Void> completionHandler) {
    redis.close(ar -> completionHandler.handle(null));
  }

  @Override
  public void get(Handler<AsyncResult<Buffer>> completionHandler) {
    redis.hgetall(field, ar -> {
      if (ar.failed()) {
        completionHandler.handle(Future.failedFuture(ar.cause()));
      } else {
        completionHandler.handle(Future.succeededFuture(Buffer.buffer(ar.result().encode())));
      }
    });
  }
}

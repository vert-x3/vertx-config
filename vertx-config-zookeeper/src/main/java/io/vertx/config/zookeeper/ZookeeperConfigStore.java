package io.vertx.config.zookeeper;

import io.vertx.config.spi.ConfigStore;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.KeeperException;

import java.util.Objects;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ZookeeperConfigStore implements ConfigStore {

  private final CuratorFramework client;
  private final String path;
  private final Vertx vertx;

  public ZookeeperConfigStore(Vertx vertx, JsonObject configuration) {
    String connection = Objects.requireNonNull(configuration.getString("connection"));
    path = Objects.requireNonNull(configuration.getString("path"));
    this.vertx = Objects.requireNonNull(vertx);
    int maxRetries = configuration.getInteger("maxRetries", 3);
    int baseGraceBetweenRetries = configuration.getInteger("baseSleepTimeBetweenRetries", 1000);

    client = CuratorFrameworkFactory.newClient(connection,
        new ExponentialBackoffRetry(baseGraceBetweenRetries, maxRetries));
    client.start();
  }

  @Override
  public void get(Handler<AsyncResult<Buffer>> completionHandler) {
    Context context = Vertx.currentContext();
    vertx.executeBlocking(
        future -> {
          try {
            client.blockUntilConnected();
            future.complete();
          } catch (InterruptedException e) {
            future.fail(e);
          }

        },
        v -> {
          if (v.failed()) {
            completionHandler.handle(Future.failedFuture(v.cause()));
          } else {
            // We are connected.
            try {
              client.getData()
                  .inBackground((client, event) -> {
                    if (context != null) {
                      context.runOnContext(x -> retrieve(event, completionHandler));
                    } else {
                      retrieve(event, completionHandler);
                    }
                  })
                  .withUnhandledErrorListener((message, e) -> {
                    Exception failure = new Exception(message, e);
                    if (context != null) {
                      context.runOnContext(x -> completionHandler.handle(Future.failedFuture(failure)));
                    } else {
                      completionHandler.handle(Future.failedFuture(failure));
                    }
                  })
                  .forPath(path);
            } catch (Exception e) {
              completionHandler.handle(Future.failedFuture(e));
            }
          }
        }
    );
  }

  private void retrieve(CuratorEvent event, Handler<AsyncResult<Buffer>> completionHandler) {
    KeeperException.Code code = KeeperException.Code.get(event.getResultCode());
    if (code == KeeperException.Code.OK) {
      completionHandler.handle(Future.succeededFuture(Buffer.buffer(event.getData())));
    } else if (code == KeeperException.Code.NONODE) {
      completionHandler.handle(Future.succeededFuture(Buffer.buffer("{}")));
    } else {
      completionHandler.handle(Future.failedFuture(KeeperException.create(code, path)));
    }
  }

  @Override
  public void close(Handler<Void> completionHandler) {
    client.close();
    completionHandler.handle(null);
  }

}

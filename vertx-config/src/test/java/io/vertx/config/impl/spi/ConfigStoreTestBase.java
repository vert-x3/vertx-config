package io.vertx.config.impl.spi;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.spi.ConfigStore;
import io.vertx.config.spi.ConfigStoreFactory;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.config.spi.ConfigProcessor;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public abstract class ConfigStoreTestBase {

  public static final ConfigProcessor JSON = new JsonProcessor();
  public static final ConfigProcessor PROPERTIES = new PropertiesConfigProcessor();

  protected Vertx vertx;
  protected ConfigStoreFactory factory;
  protected ConfigStore store;
  protected ConfigRetriever retriever;

  @Before
  public void setUp(TestContext context) {
    vertx = Vertx.vertx();
    vertx.exceptionHandler(context.exceptionHandler());
  }

  @After
  public void tearDown() {
    AtomicBoolean done = new AtomicBoolean();
    if (store != null) {
      store.close(v -> done.set(true));
      await().untilAtomic(done, is(true));
      done.set(false);
    }

    if (retriever != null) {
      retriever.close();
    }

    vertx.close(v -> done.set(true));

    await().untilAtomic(done, is(true));
  }

  protected void getJsonConfiguration(Vertx vertx, ConfigStore store, Handler<AsyncResult<JsonObject>> handler) {
    store.get(buffer -> {
      if (buffer.failed()) {
        handler.handle(Future.failedFuture(buffer.cause()));
      } else {
        JSON.process(vertx, new JsonObject(), buffer.result(), handler);
      }
    });
  }

  protected void getPropertiesConfiguration(Vertx vertx, ConfigStore store, Handler<AsyncResult<JsonObject>> handler) {
    store.get(buffer -> {
      if (buffer.failed()) {
        handler.handle(Future.failedFuture(buffer.cause()));
      } else {
        PROPERTIES.process(vertx, null, buffer.result(), handler);
      }
    });
  }
}

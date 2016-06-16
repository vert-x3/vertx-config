package io.vertx.ext.configuration.impl.spi;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.configuration.spi.ConfigurationProcessor;
import io.vertx.ext.configuration.spi.ConfigurationStore;
import io.vertx.ext.configuration.spi.ConfigurationStoreFactory;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public abstract class ConfigurationStoreTestBase {

  public static final ConfigurationProcessor JSON = new JsonProcessor();
  public static final ConfigurationProcessor PROPERTIES = new PropertiesConfigurationProcessor();

  protected Vertx vertx;
  protected ConfigurationStoreFactory factory;
  protected ConfigurationStore store;

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

    vertx.close(v -> done.set(true));

    await().untilAtomic(done, is(true));
  }

  protected void getJsonConfiguration(Vertx vertx, ConfigurationStore store, Handler<AsyncResult<JsonObject>> handler) {
    store.get(buffer -> {
      if (buffer.failed()) {
        handler.handle(Future.failedFuture(buffer.cause()));
      } else {
        JSON.process(vertx, buffer.result(), handler);
      }
    });
  }

  protected void getPropertiesConfiguration(Vertx vertx, ConfigurationStore store, Handler<AsyncResult<JsonObject>> handler) {
    store.get(buffer -> {
      if (buffer.failed()) {
        handler.handle(Future.failedFuture(buffer.cause()));
      } else {
        PROPERTIES.process(vertx, buffer.result(), handler);
      }
    });
  }
}

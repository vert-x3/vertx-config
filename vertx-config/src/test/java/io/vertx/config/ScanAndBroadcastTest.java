package io.vertx.config;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.jayway.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class ScanAndBroadcastTest {

  private Vertx vertx;
  private JsonObject http;
  private HttpServer server;
  private ConfigurationRetriever retriever;

  @Before
  public void setUp(TestContext tc) {
    vertx = Vertx.vertx();
    vertx.exceptionHandler(tc.exceptionHandler());

    http = new JsonObject();

    AtomicBoolean done = new AtomicBoolean();
    server = vertx.createHttpServer()
        .requestHandler(request -> {
          if (request.path().endsWith("/conf")) {
            request.response().end(http.encodePrettily());
          }
        })
        .listen(8080, s -> done.set(true));

    await().untilAtomic(done, is(true));
  }

  @After
  public void tearDown() {
    retriever.close();
    AtomicBoolean done = new AtomicBoolean();
    server.close(x -> done.set(true));
    await().untilAtomic(done, is(true));
    done.set(false);
    vertx.close(x -> done.set(true));
    await().untilAtomic(done, is(true));
  }

  private static List<ConfigurationStoreOptions> stores() {
    List<ConfigurationStoreOptions> options = new ArrayList<>();
    options.add(new ConfigurationStoreOptions().setType("file")
        .setConfig(new JsonObject().put("path", "src/test/resources/file/regular.json")));
    options.add(new ConfigurationStoreOptions().setType("sys")
        .setConfig(new JsonObject().put("cache", false)));
    options.add(new ConfigurationStoreOptions().setType("http")
        .setConfig(new JsonObject()
            .put("host", "localhost")
            .put("port", 8080)
            .put("path", "/conf")));
    return options;
  }

  @Test
  public void testScanning() {
    AtomicBoolean done = new AtomicBoolean();
    vertx.runOnContext(v -> {
      retriever = ConfigurationRetriever.create(vertx,
          new ConfigurationRetrieverOptions().setScanPeriod(1000).setStores(stores()));

      AtomicReference<JsonObject> current = new AtomicReference<>();
      retriever.getConfiguration(json -> {
        retriever.listen(change -> {
          if (current.get() != null  && ! current.get().equals(change.getPreviousConfiguration())) {
            throw new IllegalStateException("Previous configuration not correct");
          }
          current.set(change.getNewConfiguration());
        });
        current.set(json.result());
      });

      waitUntil(() -> current.get() != null, x -> {
        current.set(null);
        http.put("some-key", "some-value");
        waitUntil(() -> current.get() != null, x2 -> {
          assertThat(current.get().getString("some-key")).isEqualTo("some-value");
          done.set(true);
        });
      });
    });
    await().untilAtomic(done, is(true));
  }

  private void waitUntil(Callable<Boolean> condition, Handler<AsyncResult<Void>> next) {
    waitUntil(new AtomicInteger(), condition, next);
  }

  private void waitUntil(AtomicInteger counter, Callable<Boolean> condition, Handler<AsyncResult<Void>> next) {
    boolean success;
    try {
      success = condition.call();
    } catch (Exception e) {
      success = false;
    }

    if (success) {
      next.handle(Future.succeededFuture());
    } else {
      if (counter.get() >= 10000) {
        next.handle(Future.failedFuture("timeout"));
      } else {
        counter.incrementAndGet();
        vertx.setTimer(10, l -> waitUntil(counter, condition, next));
      }
    }
  }

  @Test
  public void testScanningWhenNoChanges(TestContext tc) {
    Async async = tc.async();
    vertx.runOnContext(v -> {
      retriever = ConfigurationRetriever.create(vertx,
          new ConfigurationRetrieverOptions().setScanPeriod(500).setStores(stores()));

      AtomicReference<JsonObject> current = new AtomicReference<>();
      retriever.getConfiguration(json -> {
        retriever.listen(change -> {
          if (current.get() != null  && ! current.get().equals(change.getPreviousConfiguration())) {
            throw new IllegalStateException("Previous configuration not correct");
          }
          current.set(change.getNewConfiguration());
        });
        http.put("some-key", "some-value-2");
      });

      waitUntil(() -> current.get() != null, r -> {
        if (r.failed()) {
          tc.fail(r.cause());
        } else {
          assertThat(current.get().getString("some-key")).isEqualTo("some-value-2");
          http.put("some-key", "some-value-2");
          current.set(null);

          vertx.setTimer(1000, l -> {
            assertThat(current.get()).isNull();
            async.complete();
          });
        }
      });
    });

  }

}
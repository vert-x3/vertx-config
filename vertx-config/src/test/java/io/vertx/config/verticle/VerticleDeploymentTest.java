package io.vertx.config.verticle;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Repeat;
import io.vertx.ext.unit.junit.RepeatRule;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class VerticleDeploymentTest {

  private Vertx vertx;

  @Rule
  public RepeatRule rule = new RepeatRule();

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
  }

  @After
  public void tearDown() {
    vertx.close();
  }

  @Test
  @Repeat(100)
  public void testDeploymentOfVerticles(TestContext ctxt) {
    Async async1 = ctxt.async();
    Async async2 = ctxt.async();
    ConfigRetriever retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions()
      .addStore(new ConfigStoreOptions().setType("file").setConfig(new JsonObject().put("path", "verticles.json"))));

    retriever.getConfig(json -> {
      ctxt.assertTrue(json.succeeded());
      JsonObject a = json.result().getJsonObject("a");
      JsonObject b = json.result().getJsonObject("b");

      vertx.deployVerticle(GreetingVerticle.class.getName(), new DeploymentOptions().setConfig(a), d1 -> {
        if (d1.failed()) {
          ctxt.fail(d1.cause());
        }
        vertx.deployVerticle(GreetingVerticle.class.getName(), new DeploymentOptions().setConfig(b), d2 -> {
          if (d2.failed()) {
            ctxt.fail(d2.cause());
          }

          vertx.eventBus().<String>request("greeting/hello", "", response -> {
            if (response.failed()) {
              ctxt.fail(response.cause());
              return;
            }
            ctxt.assertTrue(response.succeeded());
            String body = response.result().body();
            ctxt.assertEquals(body, "hello");
            async1.complete();
          });

          vertx.eventBus().<String>request("greeting/bonjour", "", response -> {
            if (response.failed()) {
              ctxt.fail(response.cause());
              return;
            }
            ctxt.assertTrue(response.succeeded());
            String body = response.result().body();
            ctxt.assertEquals(body, "bonjour");
            async2.complete();
          });
        });
      });

    });
  }

}

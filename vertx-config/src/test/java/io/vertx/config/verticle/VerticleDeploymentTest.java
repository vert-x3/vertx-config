package io.vertx.config.verticle;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class VerticleDeploymentTest {

  private Vertx vertx;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
  }

  @After
  public void tearDown() {
    vertx.close();
  }

  @Test
  public void testDeploymentOfVerticles(TestContext ctxt) {
    Async async1 = ctxt.async();
    Async async2 = ctxt.async();
    ConfigRetriever retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions()
      .addStore(new ConfigStoreOptions().setType("file").setConfig(new JsonObject().put("path", "verticles.json"))));

    retriever.getConfig(json -> {
      ctxt.assertTrue(json.succeeded());
      JsonObject a = json.result().getJsonObject("a");
      JsonObject b = json.result().getJsonObject("b");
      vertx.deployVerticle(GreetingVerticle.class.getName(), new DeploymentOptions().setConfig(a), ctxt.asyncAssertSuccess());
      vertx.deployVerticle(GreetingVerticle.class.getName(), new DeploymentOptions().setConfig(b), ctxt.asyncAssertSuccess());

      vertx.eventBus().<String>send("greeting/hello", "", response -> {
        ctxt.assertTrue(response.succeeded());
        String body = response.result().body();
        ctxt.assertEquals(body, "hello");
        async1.complete();
      });

      vertx.eventBus().<String>send("greeting/bonjour", "", response -> {
        ctxt.assertTrue(response.succeeded());
        String body = response.result().body();
        ctxt.assertEquals(body, "bonjour");
        async2.complete();
      });
    });
  }

}

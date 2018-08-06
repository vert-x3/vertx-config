package examples;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;

public class VerticleDeployment {

  private Vertx vertx;


  public void deploymentOfVerticles() {
    ConfigRetriever retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions()
      .addStore(new ConfigStoreOptions().setType("file").setConfig(new JsonObject().put("path", "verticles.json"))));

    retriever.getConfig(json -> {
      JsonObject a = json.result().getJsonObject("a");
      JsonObject b = json.result().getJsonObject("b");
      vertx.deployVerticle(GreetingVerticle.class.getName(), new DeploymentOptions().setConfig(a));
      vertx.deployVerticle(GreetingVerticle.class.getName(), new DeploymentOptions().setConfig(b));
    });
  }

  public void propagateConfigurationInTheEventBus() {
    ConfigRetriever retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions()
      .addStore(new ConfigStoreOptions().setType("file").setConfig(new JsonObject().put("path", "verticles.json"))));

    retriever.getConfig(json -> {
      //...
    });

    retriever.listen(change -> {
      JsonObject json = change.getNewConfiguration();
      vertx.eventBus().publish("new-configuration", json);
    });
  }

  public void configureVertx() {
    // Create a first instance of Vert.x
    Vertx vertx = Vertx.vertx();
    // Create the config retriever
    ConfigRetriever retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions()
      .addStore(new ConfigStoreOptions().setType("file").setConfig(new JsonObject().put("path", "vertx.json"))));

    // Retrieve the configuration
    retriever.getConfig(json -> {
      JsonObject result = json.result();
      // Close the vert.x instance, we don't need it anymore.
      vertx.close();

      // Create a new Vert.x instance using the retrieve configuration
      VertxOptions options = new VertxOptions(result);
      Vertx newVertx = Vertx.vertx(options);

      // Deploy your verticle
      newVertx.deployVerticle(GreetingVerticle.class.getName(), new DeploymentOptions().setConfig(result.getJsonObject("a")));
    });
  }

}

package io.vertx.config.verticle;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class MyMainVerticle extends AbstractVerticle {

  private String deploymentId;
  private ConfigRetriever configurationRetriever;

  @Override
  public void start(Future<Void> future) throws Exception {
    configurationRetriever = ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions()
            .setScanPeriod(500)
            .addStore(new ConfigStoreOptions()
                .setType("http")
                .setConfig(new JsonObject()
                    .put("host", "localhost")
                    .put("port", 8080)
                    .put("path", "/conf"))));

    configurationRetriever.listen(conf -> {
      if (deploymentId != null) {
        vertx.undeploy(deploymentId);
        deployMyVerticle(conf.getNewConfiguration(), null);
      }
    });

    // Retrieve the current configuration.
    configurationRetriever.getConfig(ar -> {
      JsonObject configuration = ar.result();
      deployMyVerticle(configuration, future);
    });
  }

  private void deployMyVerticle(JsonObject conf, Future<Void> completion) {
    vertx.deployVerticle(MyVerticle.class.getName(),
        new DeploymentOptions().setConfig(conf),
        deployed -> {
          deploymentId = deployed.result();
          if (completion != null) {
            completion.complete();
          }
        }
    );
  }

  @Override
  public void stop() throws Exception {
    configurationRetriever.close();
  }
}

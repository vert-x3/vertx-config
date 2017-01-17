package io.vertx.config.verticle;

import io.vertx.config.ConfigurationRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.config.ConfigurationRetrieverOptions;
import io.vertx.config.ConfigurationStoreOptions;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class MyMainVerticle extends AbstractVerticle {

  private String deploymentId;
  private ConfigurationRetriever configurationRetriever;

  @Override
  public void start(Future<Void> future) throws Exception {
    configurationRetriever = ConfigurationRetriever.create(vertx,
        new ConfigurationRetrieverOptions()
            .setScanPeriod(500)
            .addStore(new ConfigurationStoreOptions()
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
    configurationRetriever.getConfiguration(ar -> {
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

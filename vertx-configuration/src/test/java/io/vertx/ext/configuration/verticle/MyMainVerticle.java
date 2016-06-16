package io.vertx.ext.configuration.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.configuration.ConfigurationService;
import io.vertx.ext.configuration.ConfigurationServiceOptions;
import io.vertx.ext.configuration.ConfigurationStoreOptions;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class MyMainVerticle extends AbstractVerticle {

  private String deploymentId;
  private ConfigurationService configService;

  @Override
  public void start(Future<Void> future) throws Exception {

    configService = ConfigurationService.create(vertx,
        new ConfigurationServiceOptions()
            .setScanPeriod(500)
            .addStore(new ConfigurationStoreOptions()
                .setType("http")
                .setConfig(new JsonObject()
                    .put("host", "localhost")
                    .put("port", 8080)
                    .put("path", "/conf"))));

    configService.listen(conf -> {
      if (deploymentId != null) {
        vertx.undeploy(deploymentId);
        deployMyVerticle(conf, null);
      }
    });

    // Retrieve the current configuration.
    configService.getConfiguration(ar -> {
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
    configService.close();
  }
}

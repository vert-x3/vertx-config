/*
 * Copyright (c) 2014 Red Hat, Inc. and others
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package io.vertx.config.tests.verticle;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
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
  public void start(Promise<Void> future) throws Exception {
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
    configurationRetriever.getConfig().onComplete(ar -> {
      JsonObject configuration = ar.result();
      deployMyVerticle(configuration, future);
    });
  }

  private void deployMyVerticle(JsonObject conf, Promise<Void> completion) {
    vertx.deployVerticle(MyVerticle.class.getName(),
        new DeploymentOptions().setConfig(conf)).onComplete(deployed -> {
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

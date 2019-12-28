package examples;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class ConfigAWSExamples {

  /**
   *  Environment variables that are required to be set to use AWS
   *  AWS_ACCESS_KEY_ID
   *  AWS_SECRET_ACCESS_KEY
   * @param vertx
   */
  public void example1(Vertx vertx) {
    ConfigStoreOptions store = new ConfigStoreOptions()
      .setType("aws-secrets-manager")
      .setConfig(new JsonObject()
        .put("region", "us-west-2")
        .put("secretName", "test/example")
      );

    ConfigRetriever retriever = ConfigRetriever.create(vertx,
      new ConfigRetrieverOptions().addStore(store));
  }


}

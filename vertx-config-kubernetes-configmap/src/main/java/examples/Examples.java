package examples;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.config.ConfigurationRetriever;
import io.vertx.config.ConfigurationRetrieverOptions;
import io.vertx.config.ConfigurationStoreOptions;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Examples {


  public void example1(Vertx vertx) {
    ConfigurationStoreOptions store = new ConfigurationStoreOptions()
        .setType("configmap")
        .setConfig(new JsonObject()
            .put("namespace", "my-project-namespace")
            .put("name", "configmap-name")
        );

    ConfigurationRetriever retriever = ConfigurationRetriever.create(vertx,
        new ConfigurationRetrieverOptions().addStore(store));
  }

  public void example2(Vertx vertx) {
    ConfigurationStoreOptions store = new ConfigurationStoreOptions()
        .setType("configmap")
        .setConfig(new JsonObject()
            .put("namespace", "my-project-namespace")
            .put("name", "my-secret")
            .put("secret", true)
        );

    ConfigurationRetriever retriever = ConfigurationRetriever.create(vertx,
        new ConfigurationRetrieverOptions().addStore(store));
  }


}

package examples;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.config.ConfigStoreOptions;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Examples {


  public void example1(Vertx vertx) {
    ConfigStoreOptions store = new ConfigStoreOptions()
        .setType("configmap")
        .setConfig(new JsonObject()
            .put("namespace", "my-project-namespace")
            .put("name", "configmap-name")
        );

    ConfigRetriever retriever = ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions().addStore(store));
  }

  public void example2(Vertx vertx) {
    ConfigStoreOptions store = new ConfigStoreOptions()
        .setType("configmap")
        .setConfig(new JsonObject()
            .put("namespace", "my-project-namespace")
            .put("name", "my-secret")
            .put("secret", true)
        );

    ConfigRetriever retriever = ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions().addStore(store));
  }


}

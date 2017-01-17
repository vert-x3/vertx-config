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
        .setType("spring-config-server")
        .setConfig(new JsonObject().put("url", "http://localhost:8888/foo/development"));

    ConfigurationRetriever retriever = ConfigurationRetriever.create(vertx,
        new ConfigurationRetrieverOptions().addStore(store));
  }


}

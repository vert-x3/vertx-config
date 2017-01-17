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
        .setType("zookeeper")
        .setConfig(new JsonObject()
            .put("connection", "localhost:2181")
            .put("path", "/path/to/my/conf")
        );

    ConfigurationRetriever retriever = ConfigurationRetriever.create(vertx,
        new ConfigurationRetrieverOptions().addStore(store));
  }


}

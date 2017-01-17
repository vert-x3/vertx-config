package examples;

import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigStoreOptions;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Examples {


  public void example1(Vertx vertx) {
    ConfigStoreOptions store = new ConfigStoreOptions()
        .setType("zookeeper")
        .setConfig(new JsonObject()
            .put("connection", "localhost:2181")
            .put("path", "/path/to/my/conf")
        );

    ConfigRetriever retriever = ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions().addStore(store));
  }


}

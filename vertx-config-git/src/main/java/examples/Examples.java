package examples;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.config.ConfigStoreOptions;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Examples {


  public void example1(Vertx vertx) {

    ConfigStoreOptions git = new ConfigStoreOptions()
        .setType("git")
        .setConfig(new JsonObject()
            .put("url", "https://github.com/cescoffier/vertx-config-test.git")
            .put("path", "local")
            .put("filesets",
                new JsonArray().add(new JsonObject().put("pattern", "*.json"))));

    ConfigRetriever retriever = ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions().addStore(git));
  }


}

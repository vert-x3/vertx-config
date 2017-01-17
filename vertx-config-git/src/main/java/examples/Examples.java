package examples;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.configuration.ConfigurationRetriever;
import io.vertx.ext.configuration.ConfigurationRetrieverOptions;
import io.vertx.ext.configuration.ConfigurationStoreOptions;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Examples {


  public void example1(Vertx vertx) {

    ConfigurationStoreOptions git = new ConfigurationStoreOptions()
        .setType("git")
        .setConfig(new JsonObject()
            .put("url", "https://github.com/cescoffier/vertx-config-test.git")
            .put("path", "local")
            .put("filesets",
                new JsonArray().add(new JsonObject().put("pattern", "*.json"))));

    ConfigurationRetriever retriever = ConfigurationRetriever.create(vertx,
        new ConfigurationRetrieverOptions().addStore(git));
  }


}

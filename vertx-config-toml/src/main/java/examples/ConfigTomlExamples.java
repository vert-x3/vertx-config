package examples;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href='lyndon.codes'>Lyndon Armitage</a>
 */
public final class ConfigTomlExamples {


  public void example1(Vertx vertx) {
    JsonObject storeConfig = new JsonObject().put("path", "my-config.toml");
    ConfigStoreOptions storeOptions = new ConfigStoreOptions()
      .setType("file")
      .setFormat("toml")
      .setConfig(storeConfig);

    ConfigRetriever retriever = ConfigRetriever.create(
      vertx,
      new ConfigRetrieverOptions().addStore(storeOptions)
    );

  }

}

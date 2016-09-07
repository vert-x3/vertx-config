package examples;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.configuration.ConfigurationService;
import io.vertx.ext.configuration.ConfigurationServiceOptions;
import io.vertx.ext.configuration.ConfigurationStoreOptions;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Examples {


  public void example1(Vertx vertx) {
    ConfigurationStoreOptions store = new ConfigurationStoreOptions()
        .setType("file")
        .setConfig(new JsonObject()
            .put("path", "my-config.yaml")
            .put("format", "yaml")
        );

    ConfigurationService svc = ConfigurationService.create(vertx,
        new ConfigurationServiceOptions().addStore(store));
  }


}

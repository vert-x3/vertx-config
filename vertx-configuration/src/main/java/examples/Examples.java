package examples;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.configuration.ConfigurationService;
import io.vertx.ext.configuration.ConfigurationServiceOptions;
import io.vertx.ext.configuration.ConfigurationStoreOptions;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Examples {


  public void example1(Vertx vertx) {
    ConfigurationService svc = ConfigurationService.create(vertx);
  }

  public void example2(Vertx vertx) {
    ConfigurationStoreOptions httpStore = new ConfigurationStoreOptions()
        .setType("http")
        .setConfig(new JsonObject()
            .put("host", "localhost").put("port", 8080).put("path", "/conf"));

    ConfigurationStoreOptions fileStore = new ConfigurationStoreOptions()
        .setType("file")
        .setConfig(new JsonObject().put("path", "my-config.json"));

    ConfigurationStoreOptions sysPropsStore = new ConfigurationStoreOptions().setType("sys");


    ConfigurationServiceOptions options = new ConfigurationServiceOptions()
        .addStore(httpStore).addStore(fileStore).addStore(sysPropsStore);

    ConfigurationService svc = ConfigurationService.create(vertx, options);
  }

  public void example3(ConfigurationService svc) {
    svc.getConfiguration(ar -> {
      if (ar.failed()) {
        // Failed to retrieve the configuration
      } else {
        JsonObject config = ar.result();
      }
    });
  }

  public void file() {
    ConfigurationStoreOptions file = new ConfigurationStoreOptions()
        .setType("file")
        .setFormat("properties")
        .setConfig(new JsonObject().put("path", "path-to-file.properties"));
  }

  public void json() {
    ConfigurationStoreOptions json = new ConfigurationStoreOptions()
        .setType("json")
        .setConfig(new JsonObject().put("key", "value"));
  }

  public void sys() {
    ConfigurationStoreOptions json = new ConfigurationStoreOptions()
        .setType("sys")
        .setConfig(new JsonObject().put("cache", "false"));
  }

  public void env() {
    ConfigurationStoreOptions json = new ConfigurationStoreOptions()
        .setType("env");
  }

  public void http() {
    ConfigurationStoreOptions http = new ConfigurationStoreOptions()
        .setType("http")
        .setConfig(new JsonObject()
            .put("host", "localhost")
            .put("port", 8080)
            .put("path", "/A"));
  }

  public void http2() {
    ConfigurationStoreOptions http = new ConfigurationStoreOptions()
        .setType("http")
        .setConfig(new JsonObject()
            .put("defaultHost", "localhost")
            .put("defaultPort", 8080)
            .put("ssl", true)
            .put("path", "/A"));
  }

  public void eb() {
    ConfigurationStoreOptions eb = new ConfigurationStoreOptions()
        .setType("event-bus")
        .setConfig(new JsonObject()
            .put("address", "address-getting-the-conf")
        );
  }

  public void dir() {
    ConfigurationStoreOptions dir = new ConfigurationStoreOptions()
        .setType("directory")
        .setConfig(new JsonObject().put("path", "config")
            .put("filesets", new JsonArray()
                .add(new JsonObject().put("pattern", "dir/*json"))
                .add(new JsonObject().put("pattern", "dir/*.properties")
                    .put("format", "properties"))
            ));
  }

  public void period(ConfigurationStoreOptions store1, ConfigurationStoreOptions store2) {
    ConfigurationServiceOptions options = new ConfigurationServiceOptions()
        .setScanPeriod(2000)
        .addStore(store1)
        .addStore(store2);

    ConfigurationService svc = ConfigurationService.create(Vertx.vertx(), options);
    svc.getConfiguration(json -> {
      // Initial retrieval of the configuration
    });

    svc.listen(updatedConfig -> {
      // Configuration changes
    });
  }

  public void cache(ConfigurationService svc) {
    JsonObject last = svc.getCachedConfiguration();
  }

}

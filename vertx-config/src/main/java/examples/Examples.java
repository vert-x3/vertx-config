package examples;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Examples {


  public void example1(Vertx vertx) {
    ConfigRetriever retriever = ConfigRetriever.create(vertx);
  }

  public void example2(Vertx vertx) {
    ConfigStoreOptions httpStore = new ConfigStoreOptions()
      .setType("http")
      .setConfig(new JsonObject()
        .put("host", "localhost").put("port", 8080).put("path", "/conf"));

    ConfigStoreOptions fileStore = new ConfigStoreOptions()
      .setType("file")
      .setConfig(new JsonObject().put("path", "my-config.json"));

    ConfigStoreOptions sysPropsStore = new ConfigStoreOptions().setType("sys");


    ConfigRetrieverOptions options = new ConfigRetrieverOptions()
      .addStore(httpStore).addStore(fileStore).addStore(sysPropsStore);

    ConfigRetriever retriever = ConfigRetriever.create(vertx, options);
  }

  public void example3(ConfigRetriever retriever) {
    retriever.getConfig(ar -> {
      if (ar.failed()) {
        // Failed to retrieve the configuration
      } else {
        JsonObject config = ar.result();
      }
    });
  }

  public void future(ConfigRetriever retriever) {
    Future<JsonObject> future = ConfigRetriever.getConfigAsFuture(retriever);
    future.setHandler(ar -> {
      if (ar.failed()) {
        // Failed to retrieve the configuration
      } else {
        JsonObject config = ar.result();
      }
    });
  }

  public void file() {
    ConfigStoreOptions file = new ConfigStoreOptions()
      .setType("file")
      .setFormat("properties")
      .setConfig(new JsonObject().put("path", "path-to-file.properties"));
  }

  public void json() {
    ConfigStoreOptions json = new ConfigStoreOptions()
      .setType("json")
      .setConfig(new JsonObject().put("key", "value"));
  }

  public void sys() {
    ConfigStoreOptions json = new ConfigStoreOptions()
      .setType("sys")
      .setConfig(new JsonObject().put("cache", "false"));
  }

  public void env() {
    ConfigStoreOptions json = new ConfigStoreOptions()
      .setType("env");
  }

  public void env2() {
    ConfigStoreOptions json = new ConfigStoreOptions()
      .setType("env")
      .setConfig(new JsonObject().put("raw-data", true));
  }



  public void http() {
    ConfigStoreOptions http = new ConfigStoreOptions()
      .setType("http")
      .setConfig(new JsonObject()
        .put("host", "localhost")
        .put("port", 8080)
        .put("path", "/A"));
  }

  public void http2() {
    ConfigStoreOptions http = new ConfigStoreOptions()
      .setType("http")
      .setConfig(new JsonObject()
        .put("defaultHost", "localhost")
        .put("defaultPort", 8080)
        .put("ssl", true)
        .put("path", "/A"));
  }

  public void eb() {
    ConfigStoreOptions eb = new ConfigStoreOptions()
      .setType("event-bus")
      .setConfig(new JsonObject()
        .put("address", "address-getting-the-conf")
      );
  }

  public void dir() {
    ConfigStoreOptions dir = new ConfigStoreOptions()
      .setType("directory")
      .setConfig(new JsonObject().put("path", "config")
        .put("filesets", new JsonArray()
          .add(new JsonObject().put("pattern", "dir/*json"))
          .add(new JsonObject().put("pattern", "dir/*.properties")
            .put("format", "properties"))
        ));
  }

  public void consul() {
    ConfigStoreOptions consul = new ConfigStoreOptions()
      .setType("consul")
      .setConfig(new JsonObject()
        .put("prefix", "foo"));
  }

  public void period(ConfigStoreOptions store1, ConfigStoreOptions store2) {
    ConfigRetrieverOptions options = new ConfigRetrieverOptions()
      .setScanPeriod(2000)
      .addStore(store1)
      .addStore(store2);

    ConfigRetriever retriever = ConfigRetriever.create(Vertx.vertx(), options);
    retriever.getConfig(json -> {
      // Initial retrieval of the configuration
    });

    retriever.listen(change -> {
      // Previous configuration
      JsonObject previous = change.getPreviousConfiguration();
      // New configuration
      JsonObject conf = change.getNewConfiguration();
    });
  }

  public void stream(ConfigStoreOptions store1, ConfigStoreOptions store2) {
    ConfigRetrieverOptions options = new ConfigRetrieverOptions()
      .setScanPeriod(2000)
      .addStore(store1)
      .addStore(store2);

    ConfigRetriever retriever = ConfigRetriever.create(Vertx.vertx(), options);
    retriever.configStream()
      .endHandler(v -> {
        // retriever closed
      })
      .exceptionHandler(t -> {
        // an error has been caught while retrieving the configuration
      })
      .handler(conf -> {
        // the configuration
      });

  }

  public void cache(ConfigRetriever retriever) {
    JsonObject last = retriever.getCachedConfig();
  }

}

package io.vertx.config;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@DataObject(generateConverter = true)
public class ConfigRetrieverOptions {

  private static final long SCAN_PERIOD_DEFAULT = 5000L;

  private long scanPeriod = SCAN_PERIOD_DEFAULT;

  private List<ConfigStoreOptions> stores = new ArrayList<>();

  public ConfigRetrieverOptions() {
    // Empty constructor
  }

  public ConfigRetrieverOptions(ConfigRetrieverOptions other) {
    this.scanPeriod = other.scanPeriod;
    this.stores = other.stores;
  }

  public ConfigRetrieverOptions(JsonObject json) {
    ConfigRetrieverOptionsConverter.fromJson(json, this);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    ConfigRetrieverOptionsConverter.toJson(this, json);
    return json;
  }

  public long getScanPeriod() {
    return scanPeriod;
  }

  public ConfigRetrieverOptions setScanPeriod(long scanPeriod) {
    this.scanPeriod = scanPeriod;
    return this;
  }

  public List<ConfigStoreOptions> getStores() {
    return stores;
  }

  public ConfigRetrieverOptions setStores(List<ConfigStoreOptions> stores) {
    if (stores == null) {
      this.stores = new ArrayList<>();
    } else {
      this.stores = stores;
    }
    return this;
  }

  public ConfigRetrieverOptions addStore(ConfigStoreOptions options) {
    getStores().add(options);
    return this;
  }
}

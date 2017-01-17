package io.vertx.config;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@DataObject(generateConverter = true)
public class ConfigurationRetrieverOptions {

  private static final long SCAN_PERIOD_DEFAULT = 5000L;

  private long scanPeriod = SCAN_PERIOD_DEFAULT;

  private List<ConfigurationStoreOptions> stores = new ArrayList<>();

  public ConfigurationRetrieverOptions() {
    // Empty constructor
  }

  public ConfigurationRetrieverOptions(ConfigurationRetrieverOptions other) {
    this.scanPeriod = other.scanPeriod;
    this.stores = other.stores;
  }

  public ConfigurationRetrieverOptions(JsonObject json) {
    ConfigurationRetrieverOptionsConverter.fromJson(json, this);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    ConfigurationRetrieverOptionsConverter.toJson(this, json);
    return json;
  }

  public long getScanPeriod() {
    return scanPeriod;
  }

  public ConfigurationRetrieverOptions setScanPeriod(long scanPeriod) {
    this.scanPeriod = scanPeriod;
    return this;
  }

  public List<ConfigurationStoreOptions> getStores() {
    return stores;
  }

  public ConfigurationRetrieverOptions setStores(List<ConfigurationStoreOptions> stores) {
    if (stores == null) {
      this.stores = new ArrayList<>();
    } else {
      this.stores = stores;
    }
    return this;
  }

  public ConfigurationRetrieverOptions addStore(ConfigurationStoreOptions options) {
    getStores().add(options);
    return this;
  }
}

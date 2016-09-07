package io.vertx.ext.configuration;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@DataObject(generateConverter = true)
public class ConfigurationServiceOptions {

  private static final long SCAN_PERIOD_DEFAULT = 5000L;

  private long scanPeriod = SCAN_PERIOD_DEFAULT;

  private List<ConfigurationStoreOptions> stores = new ArrayList<>();

  public ConfigurationServiceOptions() {
    // Empty constructor
  }

  public ConfigurationServiceOptions(ConfigurationServiceOptions other) {
    this.scanPeriod = other.scanPeriod;
    this.stores = other.stores;
  }

  public ConfigurationServiceOptions(JsonObject json) {
    ConfigurationServiceOptionsConverter.fromJson(json, this);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    ConfigurationServiceOptionsConverter.toJson(this, json);
    return json;
  }

  public long getScanPeriod() {
    return scanPeriod;
  }

  public ConfigurationServiceOptions setScanPeriod(long scanPeriod) {
    this.scanPeriod = scanPeriod;
    return this;
  }

  public List<ConfigurationStoreOptions> getStores() {
    return stores;
  }

  public ConfigurationServiceOptions setStores(List<ConfigurationStoreOptions> stores) {
    if (stores == null) {
      this.stores = new ArrayList<>();
    } else {
      this.stores = stores;
    }
    return this;
  }

  public ConfigurationServiceOptions addStore(ConfigurationStoreOptions options) {
    getStores().add(options);
    return this;
  }
}

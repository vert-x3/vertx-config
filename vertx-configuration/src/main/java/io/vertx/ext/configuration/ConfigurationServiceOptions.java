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

  private static final String BROADCAST_ADDRESS_DEFAULT = "configuration";
  private static final long SCAN_PERIOD_DEFAULT = 5000L;

  private long scanPeriod = SCAN_PERIOD_DEFAULT;

  private String broadcastAddress = BROADCAST_ADDRESS_DEFAULT;

  private List<ConfigurationStoreOptions> stores = new ArrayList<>();

  public ConfigurationServiceOptions() {
    // Empty constructor
  }

  public ConfigurationServiceOptions(ConfigurationServiceOptions other) {
    this.scanPeriod = other.scanPeriod;
    this.broadcastAddress = other.broadcastAddress;
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

  public String getBroadcastAddress() {
    return broadcastAddress;
  }

  public ConfigurationServiceOptions setBroadcastAddress(String broadcastAddress) {
    this.broadcastAddress = broadcastAddress;
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

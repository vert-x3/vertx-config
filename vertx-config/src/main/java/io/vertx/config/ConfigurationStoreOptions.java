package io.vertx.config;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.Objects;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@DataObject
public class ConfigurationStoreOptions {

  private String type;

  private JsonObject config;
  private String format;

  public ConfigurationStoreOptions() {
    // Empty constructor
  }

  public ConfigurationStoreOptions(ConfigurationStoreOptions other) {
    this.type = other.type;
    this.config = other.config;
  }

  public ConfigurationStoreOptions(JsonObject json) {
    type = json.getString("type");
    config = json.getJsonObject("config", new JsonObject());
    format = json.getString("format", "json");
  }


  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    if (type != null) {
      json.put("type", type);
    }
    if (config != null) {
      json.put("config", config);
    }
    if (format != null) {
      json.put("format", format);
    }
    return json;
  }

  public String getType() {
    return type;
  }

  public ConfigurationStoreOptions setType(String type) {
    this.type = type;
    return this;
  }

  public JsonObject getConfig() {
    //TODO can contain store and format config
    return config;
  }

  public ConfigurationStoreOptions setConfig(JsonObject config) {
    this.config = config;
    return this;
  }


  public String getFormat() {
    return format;
  }

  public ConfigurationStoreOptions setFormat(String format) {
    Objects.requireNonNull(format);
    this.format = format;
    return this;
  }
}

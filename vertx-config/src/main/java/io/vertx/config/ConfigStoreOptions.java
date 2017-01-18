package io.vertx.config;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.Objects;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@DataObject
public class ConfigStoreOptions {

  private String type;

  private JsonObject config;
  private String format;

  public ConfigStoreOptions() {
    // Empty constructor
  }

  public ConfigStoreOptions(ConfigStoreOptions other) {
    this.type = other.type;
    this.config = other.config;
  }

  public ConfigStoreOptions(JsonObject json) {
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

  public ConfigStoreOptions setType(String type) {
    this.type = type;
    return this;
  }

  public JsonObject getConfig() {
    //TODO can contain store and format config
    return config;
  }

  public ConfigStoreOptions setConfig(JsonObject config) {
    this.config = config;
    return this;
  }


  public String getFormat() {
    return format;
  }

  public ConfigStoreOptions setFormat(String format) {
    Objects.requireNonNull(format);
    this.format = format;
    return this;
  }
}

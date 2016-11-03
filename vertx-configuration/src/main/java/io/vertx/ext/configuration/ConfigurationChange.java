package io.vertx.ext.configuration;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * A structure representing a configuration change.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@DataObject
public class ConfigurationChange {

  private JsonObject previousConfiguration;

  private JsonObject newConfiguration;

  public ConfigurationChange(JsonObject prevConf, JsonObject newConf) {
    setPreviousConfiguration(prevConf);
    setNewConfiguration(newConf);
  }

  public JsonObject getPreviousConfiguration() {
    return previousConfiguration;
  }

  public ConfigurationChange setPreviousConfiguration(JsonObject conf) {
    if (conf == null) {
      this.previousConfiguration = new JsonObject();
    } else {
      this.previousConfiguration = conf;
    }
    return this;
  }

  public JsonObject getNewConfiguration() {
    return newConfiguration;
  }

  public ConfigurationChange setNewConfiguration(JsonObject conf) {
    if (conf == null) {
      this.newConfiguration = new JsonObject();
    } else {
      this.newConfiguration = conf;
    }
    return this;
  }

  public ConfigurationChange() {
    newConfiguration = new JsonObject();
    previousConfiguration = new JsonObject();
  }

  public ConfigurationChange(ConfigurationChange other) {
    this.previousConfiguration = other.previousConfiguration;
    this.newConfiguration = other.newConfiguration;
  }

  public ConfigurationChange(JsonObject json) {
    this.setNewConfiguration(json.getJsonObject("newConfiguration", new JsonObject()));
    this.setPreviousConfiguration(json.getJsonObject("previousConfiguration", new JsonObject()));
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.put("newConfiguration", newConfiguration);
    json.put("previousConfiguration", previousConfiguration);
    return json;
  }
}

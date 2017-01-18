package io.vertx.config;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * A structure representing a configuration change.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@DataObject
public class ConfigChange {

  private JsonObject previousConfiguration;

  private JsonObject newConfiguration;

  public ConfigChange(JsonObject prevConf, JsonObject newConf) {
    setPreviousConfiguration(prevConf);
    setNewConfiguration(newConf);
  }

  public JsonObject getPreviousConfiguration() {
    return previousConfiguration;
  }

  public ConfigChange setPreviousConfiguration(JsonObject conf) {
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

  public ConfigChange setNewConfiguration(JsonObject conf) {
    if (conf == null) {
      this.newConfiguration = new JsonObject();
    } else {
      this.newConfiguration = conf;
    }
    return this;
  }

  public ConfigChange() {
    newConfiguration = new JsonObject();
    previousConfiguration = new JsonObject();
  }

  public ConfigChange(ConfigChange other) {
    this.previousConfiguration = other.previousConfiguration;
    this.newConfiguration = other.newConfiguration;
  }

  public ConfigChange(JsonObject json) {
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

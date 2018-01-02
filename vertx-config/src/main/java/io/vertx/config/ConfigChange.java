/*
 * Copyright (c) 2014 Red Hat, Inc. and others
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package io.vertx.config;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

import java.util.Objects;

/**
 * A structure representing a configuration change.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@DataObject
public class ConfigChange {

  private JsonObject previousConfiguration;

  private JsonObject newConfiguration;

  /**
   * Creates a new {@link ConfigChange} instance.
   *
   * @param prevConf the previous configuration, may be {@code null}. In this case, an empty JSON object is used.
   * @param newConf  the new configuration, may be {@code null}. In this case, an empty JSON object is used.
   */
  public ConfigChange(JsonObject prevConf, JsonObject newConf) {
    setPreviousConfiguration(prevConf);
    setNewConfiguration(newConf);
  }

  /**
   * @return the previous configuration, never {@code null}, but potentially empty
   */
  public JsonObject getPreviousConfiguration() {
    return previousConfiguration;
  }

  /**
   * Sets the previous configuration.
   *
   * @param conf the configuration, may be {@code null}. In this case an empty JSON object is used.
   * @return the current instance of {@link ConfigChange}
   */
  public ConfigChange setPreviousConfiguration(JsonObject conf) {
    if (conf == null) {
      this.previousConfiguration = new JsonObject();
    } else {
      this.previousConfiguration = conf;
    }
    return this;
  }

  /**
   * @return the new configuration, never {@code null}, but potentially empty.
   */
  public JsonObject getNewConfiguration() {
    return newConfiguration;
  }

  /**
   * Sets the new configuration.
   *
   * @param conf the new configuration, may be {@code null}. In this case, an empty JSON object is used.
   * @return the current instance of {@link ConfigChange}
   */
  public ConfigChange setNewConfiguration(JsonObject conf) {
    if (conf == null) {
      this.newConfiguration = new JsonObject();
    } else {
      this.newConfiguration = conf;
    }
    return this;
  }

  /**
   * Creates a new instance from {@link ConfigChange} using empty JSON Object for both the old and new configuration.
   */
  public ConfigChange() {
    newConfiguration = new JsonObject();
    previousConfiguration = new JsonObject();
  }

  /**
   * Creates a new instance of {@link ConfigChange} copying the values stored in the given object.
   *
   * @param other the instance to copy
   */
  public ConfigChange(ConfigChange other) {
    this.previousConfiguration = other.previousConfiguration.copy();
    this.newConfiguration = other.newConfiguration.copy();
  }

  /**
   * Creates a new {@link ConfigChange} instance from the given JSON object.
   *
   * @param json the json object, must not be {@code null}
   */
  public ConfigChange(JsonObject json) {
    Objects.requireNonNull(json);
    this.setNewConfiguration(json.getJsonObject("newConfiguration", new JsonObject()));
    this.setPreviousConfiguration(json.getJsonObject("previousConfiguration", new JsonObject()));
  }

  /**
   * @return the JSON representation of the current {@link ConfigChange} instance.
   */
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.put("newConfiguration", newConfiguration);
    json.put("previousConfiguration", previousConfiguration);
    return json;
  }
}

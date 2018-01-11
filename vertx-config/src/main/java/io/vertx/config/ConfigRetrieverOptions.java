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
  private boolean includeDefaultStores = false;

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

  public boolean isIncludeDefaultStores() {
    return includeDefaultStores;
  }

  public ConfigRetrieverOptions setIncludeDefaultStores(boolean includeDefaultStores) {
    this.includeDefaultStores = includeDefaultStores;
    return this;
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

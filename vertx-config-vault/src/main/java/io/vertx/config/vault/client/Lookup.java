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

package io.vertx.config.vault.client;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Map;

/**
 * Represents Lookup result
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Lookup {

  @JsonProperty("accessor")
  private String accessor;

  @JsonProperty(value = "creation_time", defaultValue = "0")
  private long creationTime;

  @JsonProperty(value = "creation_ttl", defaultValue = "0")
  private long creationTTL;

  @JsonProperty("display_name")
  private String displayName;

  @JsonProperty(value = "explicit_max_ttl", defaultValue = "0")
  private long explicitMaxTTL;

  @JsonProperty("id")
  private String id;

  @JsonProperty(value = "last_renewal_time", defaultValue = "-1")
  private long lastRenewalTime;

  @JsonProperty(value = "num_uses", defaultValue = "0")
  private long numUses;

  @JsonProperty(value = "orphan", defaultValue = "true")
  private boolean orphan;

  @JsonProperty(value = "path")
  private String path;

  @JsonProperty("policies")
  private List<String> policies;

  @JsonProperty("ttl")
  private long ttl;

  @JsonProperty("renewable")
  private boolean renewable;

  private JsonObject metadata;

  public String getAccessor() {
    return accessor;
  }

  public long getCreationTime() {
    return creationTime;
  }

  public long getCreationTTL() {
    return creationTTL;
  }

  public String getDisplayName() {
    return displayName;
  }

  public long getExplicitMaxTTL() {
    return explicitMaxTTL;
  }

  public String getId() {
    return id;
  }

  public long getLastRenewalTime() {
    return lastRenewalTime;
  }

  public long getNumUses() {
    return numUses;
  }

  public boolean isOrphan() {
    return orphan;
  }

  public String getPath() {
    return path;
  }

  public List<String> getPolicies() {
    return policies;
  }

  public long getTTL() {
    return ttl;
  }

  public boolean isRenewable() {
    return renewable;
  }

  public JsonObject getMetadata() {
    return metadata;
  }

  @JsonProperty("metadata")
  private void setMetadata(Map<String, Object> meta) {
    this.metadata = new JsonObject(meta);
  }

  public String getUsername() {
    return getMetadata() != null ? getMetadata().getString("username") : null;
  }
}

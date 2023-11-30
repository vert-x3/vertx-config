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

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.JsonGen;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * Represents Lookup result
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@DataObject
@JsonGen(publicConverter = false, jsonPropertyNameFormatter = io.vertx.codegen.format.SnakeCase.class)
public class Lookup {

  private String accessor;

  private long creationTime = 0;

  private long creationTTL = 0;

  private String displayName;

  private long explicitMaxTTL = 0;

  private String id;

  private long lastRenewalTime = -1;

  private long numUses = 0;

  private boolean orphan = true;

  private String path;

  private List<String> policies;

  private long ttl;

  private boolean renewable;

  private JsonObject metadata;

  public Lookup() {
  }

  public Lookup(JsonObject json) {
    LookupConverter.fromJson(json, this);
  }

  public String getAccessor() {
    return accessor;
  }

  public Lookup setAccessor(String accessor) {
    this.accessor = accessor;
    return this;
  }

  public long getCreationTime() {
    return creationTime;
  }

  public Lookup setCreationTime(long creationTime) {
    this.creationTime = creationTime;
    return this;
  }

  public long getCreationTTL() {
    return creationTTL;
  }

  public Lookup setCreationTTL(long creationTTL) {
    this.creationTTL = creationTTL;
    return this;
  }

  public String getDisplayName() {
    return displayName;
  }

  public Lookup setDisplayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  public long getExplicitMaxTTL() {
    return explicitMaxTTL;
  }

  public Lookup setExplicitMaxTTL(long explicitMaxTTL) {
    this.explicitMaxTTL = explicitMaxTTL;
    return this;
  }

  public String getId() {
    return id;
  }

  public Lookup setId(String id) {
    this.id = id;
    return this;
  }

  public long getLastRenewalTime() {
    return lastRenewalTime;
  }

  public Lookup setLastRenewalTime(long lastRenewalTime) {
    this.lastRenewalTime = lastRenewalTime;
    return this;
  }

  public long getNumUses() {
    return numUses;
  }

  public Lookup setNumUses(long numUses) {
    this.numUses = numUses;
    return this;
  }

  public boolean isOrphan() {
    return orphan;
  }

  public Lookup setOrphan(boolean orphan) {
    this.orphan = orphan;
    return this;
  }

  public String getPath() {
    return path;
  }

  public Lookup setPath(String path) {
    this.path = path;
    return this;
  }

  public List<String> getPolicies() {
    return policies;
  }

  public Lookup setPolicies(List<String> policies) {
    this.policies = policies;
    return this;
  }

  public long getTtl() {
    return ttl;
  }

  public Lookup setTtl(long ttl) {
    this.ttl = ttl;
    return this;
  }

  public boolean isRenewable() {
    return renewable;
  }

  public Lookup setRenewable(boolean renewable) {
    this.renewable = renewable;
    return this;
  }

  public JsonObject getMetadata() {
    return metadata;
  }

  public Lookup setMetadata(JsonObject metadata) {
    this.metadata = metadata;
    return this;
  }

  public JsonObject toJson() {
    final JsonObject json = new JsonObject();
    LookupConverter.toJson(this, json);
    return json;
  }

  @Override
  public String toString() {
    return toJson().encode();
  }
}

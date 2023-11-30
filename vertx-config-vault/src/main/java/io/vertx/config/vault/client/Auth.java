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
import io.vertx.codegen.json.annotations.JsonGen;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Map;

/**
 * Represents Auth result.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@DataObject
@JsonGen(publicConverter = false, jsonPropertyNameFormatter = io.vertx.codegen.format.SnakeCase.class)
public class Auth {

  private String accessor;

  private long leaseDuration;

  private boolean renewable;

  private JsonObject metadata;

  private String clientToken;

  private List<String> policies;

  public Auth() {
  }

  public Auth(JsonObject json) {
    AuthConverter.fromJson(json, this);
  }

  public String getAccessor() {
    return accessor;
  }

  public Auth setAccessor(String accessor) {
    this.accessor = accessor;
    return this;
  }

  public long getLeaseDuration() {
    return leaseDuration;
  }

  public Auth setLeaseDuration(long leaseDuration) {
    this.leaseDuration = leaseDuration;
    return this;
  }

  public boolean isRenewable() {
    return renewable;
  }

  public Auth setRenewable(boolean renewable) {
    this.renewable = renewable;
    return this;
  }

  public JsonObject getMetadata() {
    return metadata;
  }

  public Auth setMetadata(JsonObject metadata) {
    this.metadata = metadata;
    return this;
  }

  public String getClientToken() {
    return clientToken;
  }

  public Auth setClientToken(String clientToken) {
    this.clientToken = clientToken;
    return this;
  }

  public List<String> getPolicies() {
    return policies;
  }

  public Auth setPolicies(List<String> policies) {
    this.policies = policies;
    return this;
  }

  public String getApplicationId() {
    return getMetadata() != null ? getMetadata().getString("app-id") : null;
  }

  public String getUserId() {
    return getMetadata() != null ? getMetadata().getString("user-id") : null;
  }

  public String getUsername() {
    return getMetadata() != null ? getMetadata().getString("username") : null;
  }

  public JsonObject toJson() {
    final JsonObject json = new JsonObject();
    AuthConverter.toJson(this, json);
    return json;
  }


  @Override
  public String toString() {
    return toJson().encode();
  }
}

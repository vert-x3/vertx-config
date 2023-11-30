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

import java.util.Map;

/**
 * Represent Secret result.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@DataObject
@JsonGen(publicConverter = false, jsonPropertyNameFormatter = io.vertx.codegen.format.SnakeCase.class)
public class Secret {

  private String leaseId;

  private boolean renewable;

  private long leaseDuration;

  private String requestId;

  private JsonObject data;

  public Secret() {
  }

  public Secret(JsonObject json) {
    SecretConverter.fromJson(json, this);
  }

  public String getLeaseId() {
    return leaseId;
  }

  public Secret setLeaseId(String leaseId) {
    this.leaseId = leaseId;
    return this;
  }

  public boolean isRenewable() {
    return renewable;
  }

  public Secret setRenewable(boolean renewable) {
    this.renewable = renewable;
    return this;
  }

  public long getLeaseDuration() {
    return leaseDuration;
  }

  public Secret setLeaseDuration(long leaseDuration) {
    this.leaseDuration = leaseDuration;
    return this;
  }

  public String getRequestId() {
    return requestId;
  }

  public Secret setRequestId(String requestId) {
    this.requestId = requestId;
    return this;
  }

  public JsonObject getData() {
    return data;
  }

  public Secret setData(JsonObject data) {
    this.data = data;
    if (this.data != null) {
      // unwrap the data
      if (this.data.containsKey("data")) {
        this.data = data.getJsonObject("data");
      }
    }
    return this;
  }

  public JsonObject toJson() {
    final JsonObject json = new JsonObject();
    SecretConverter.toJson(this, json);
    return json;
  }

  @Override
  public String toString() {
    return toJson().encode();
  }
}

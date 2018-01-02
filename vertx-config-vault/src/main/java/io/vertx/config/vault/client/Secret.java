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

import java.util.Map;

/**
 * Represent Secret result.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Secret {

  @JsonProperty("lease_id")
  private String leaseId;

  @JsonProperty("renewable")
  private boolean renewable;

  @JsonProperty("lease_duration")
  private long leaseDuration;

  @JsonProperty("request_id")
  private String requestId;

  private JsonObject data;

  public String getLeaseId() {
    return leaseId;
  }

  public boolean isRenewable() {
    return renewable;
  }

  public long getLeaseDuration() {
    return leaseDuration;
  }

  public JsonObject getData() {
    return data;
  }

  @JsonProperty("data")
  private void setData(Map<String, Object> data) {
    this.data = new JsonObject(data);
  }

  public String getRequestId() {
    return requestId;
  }
}

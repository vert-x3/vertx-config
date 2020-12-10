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

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Map;

/**
 * The token request structure.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class TokenRequest {

  /**
   * The ID of the client token. Can only be specified by a root token. Otherwise, the token ID is a randomly
   * generated UUID.
   */
  private String id;

  /**
   * A list of policies for the token. This must be a subset of the policies belonging to the token.
   */
  private List<String> policies;

  /**
   * A map of string to string valued metadata. This is passed through to the audit backends.
   */
  private Map<String, String> meta;

  /**
   * If true and set by a root caller, the token will not have the parent token of the caller. This creates a token with
   * no parent.
   */
  private boolean noParent;

  /**
   * If <code>true</code> the default policy will not be a part of this token's policy set.
   */
  private boolean noDefaultPolicy;

  /**
   * The TTL period of the token, provided as "1h", where hour is the largest suffix. If not provided, the token is
   * valid for the default lease TTL, or indefinitely if the root policy is used.
   */
  private String ttl;

  /**
   * The display name of the token. Defaults to "token".
   */
  private String displayName;


  /**
   * The maximum uses for the given token. This can be used to create a one-time-token or limited use token.
   * Defaults to 0, which has no limit to the number of uses.
   */
  private long numUses;

  /**
   * The role the token will be created with. Default is no role.
   */
  private String role;

  public TokenRequest() {
  }

  public TokenRequest(JsonObject json) {
    fromJson(json, this);
  }

  public String getId() {
    return id;
  }

  public TokenRequest setId(String id) {
    this.id = id;
    return this;
  }

  public List<String> getPolicies() {
    return policies;
  }

  public TokenRequest setPolicies(List<String> policies) {
    this.policies = policies;
    return this;
  }

  public Map<String, String> getMeta() {
    return meta;
  }

  public TokenRequest setMeta(Map<String, String> meta) {
    this.meta = meta;
    return this;
  }

  public boolean isNoParent() {
    return noParent;
  }

  public TokenRequest setNoParent(boolean noParent) {
    this.noParent = noParent;
    return this;
  }

  public boolean isNoDefaultPolicy() {
    return noDefaultPolicy;
  }

  public TokenRequest setNoDefaultPolicy(boolean noDefaultPolicy) {
    this.noDefaultPolicy = noDefaultPolicy;
    return this;
  }

  public String getTtl() {
    return ttl;
  }

  public TokenRequest setTTL(String ttl) {
    this.ttl = ttl;
    return this;
  }

  public String getDisplayName() {
    return displayName;
  }

  public TokenRequest setDisplayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  public long getNumUses() {
    return numUses;
  }

  public TokenRequest setNumUses(long numUses) {
    this.numUses = numUses;
    return this;
  }

  public String getRole() {
    return role;
  }

  public TokenRequest setRole(String role) {
    this.role = role;
    return this;
  }

  public JsonObject toPayload() {
    JsonObject payload = new JsonObject();
    if (getId() != null) {
      payload.put("id", getId());
    }
    if (getPolicies() != null && !getPolicies().isEmpty()) {
      JsonArray policies = new JsonArray();
      getPolicies().forEach(policies::add);
      payload.put("policies", policies);
    }

    if (getMeta() != null && !getMeta().isEmpty()) {
      JsonObject meta = new JsonObject();
      getMeta().forEach(meta::put);
      payload.put("meta", meta);
    }

    if (isNoParent()) {
      payload.put("no_parent", isNoParent());
    }

    if (isNoDefaultPolicy()) {
      payload.put("no_default_policy", isNoDefaultPolicy());
    }

    if (getTtl() != null) {
      payload.put("ttl", getTtl());
    }

    if (getDisplayName() != null) {
      payload.put("display_name", getDisplayName());
    }

    if (getNumUses() != 0) payload.put("num_uses", getNumUses());

    return payload;
  }

  private static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, TokenRequest obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "displayName":
          if (member.getValue() instanceof String) {
            obj.setDisplayName((String)member.getValue());
          }
          break;
        case "id":
          if (member.getValue() instanceof String) {
            obj.setId((String)member.getValue());
          }
          break;
        case "meta":
          if (member.getValue() instanceof JsonObject) {
            java.util.Map<String, java.lang.String> map = new java.util.LinkedHashMap<>();
            ((Iterable<java.util.Map.Entry<String, Object>>)member.getValue()).forEach(entry -> {
              if (entry.getValue() instanceof String)
                map.put(entry.getKey(), (String)entry.getValue());
            });
            obj.setMeta(map);
          }
          break;
        case "noDefaultPolicy":
          if (member.getValue() instanceof Boolean) {
            obj.setNoDefaultPolicy((Boolean)member.getValue());
          }
          break;
        case "noParent":
          if (member.getValue() instanceof Boolean) {
            obj.setNoParent((Boolean)member.getValue());
          }
          break;
        case "numUses":
          if (member.getValue() instanceof Number) {
            obj.setNumUses(((Number)member.getValue()).longValue());
          }
          break;
        case "policies":
          if (member.getValue() instanceof JsonArray) {
            java.util.ArrayList<java.lang.String> list =  new java.util.ArrayList<>();
            ((Iterable<Object>)member.getValue()).forEach( item -> {
              if (item instanceof String)
                list.add((String)item);
            });
            obj.setPolicies(list);
          }
          break;
        case "role":
          if (member.getValue() instanceof String) {
            obj.setRole((String)member.getValue());
          }
          break;
        case "ttl":
          if (member.getValue() instanceof String) {
            obj.setTTL((String)member.getValue());
          }
          break;
      }
    }
  }}

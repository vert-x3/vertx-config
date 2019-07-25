package io.vertx.config.vault.client;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import io.vertx.core.spi.json.JsonCodec;

/**
 * Converter and Codec for {@link io.vertx.config.vault.client.TokenRequest}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.config.vault.client.TokenRequest} original class using Vert.x codegen.
 */
public class TokenRequestConverter implements JsonCodec<TokenRequest, JsonObject> {

  public static final TokenRequestConverter INSTANCE = new TokenRequestConverter();

  @Override public JsonObject encode(TokenRequest value) { return (value != null) ? value.toJson() : null; }

  @Override public TokenRequest decode(JsonObject value) { return (value != null) ? new TokenRequest(value) : null; }

  @Override public Class<TokenRequest> getTargetClass() { return TokenRequest.class; }

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, TokenRequest obj) {
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
  }

  public static void toJson(TokenRequest obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(TokenRequest obj, java.util.Map<String, Object> json) {
    if (obj.getDisplayName() != null) {
      json.put("displayName", obj.getDisplayName());
    }
    if (obj.getId() != null) {
      json.put("id", obj.getId());
    }
    if (obj.getMeta() != null) {
      JsonObject map = new JsonObject();
      obj.getMeta().forEach((key, value) -> map.put(key, value));
      json.put("meta", map);
    }
    json.put("noDefaultPolicy", obj.isNoDefaultPolicy());
    json.put("noParent", obj.isNoParent());
    json.put("numUses", obj.getNumUses());
    if (obj.getPolicies() != null) {
      JsonArray array = new JsonArray();
      obj.getPolicies().forEach(item -> array.add(item));
      json.put("policies", array);
    }
    if (obj.getRole() != null) {
      json.put("role", obj.getRole());
    }
    if (obj.getTtl() != null) {
      json.put("ttl", obj.getTtl());
    }
  }
}

package io.vertx.config.vault.client;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.impl.JsonUtil;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Converter and mapper for {@link io.vertx.config.vault.client.Auth}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.config.vault.client.Auth} original class using Vert.x codegen.
 */
public class AuthConverter {


  private static final Base64.Decoder BASE64_DECODER = JsonUtil.BASE64_DECODER;
  private static final Base64.Encoder BASE64_ENCODER = JsonUtil.BASE64_ENCODER;

  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, Auth obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "accessor":
          if (member.getValue() instanceof String) {
            obj.setAccessor((String)member.getValue());
          }
          break;
        case "application_id":
          break;
        case "client_token":
          if (member.getValue() instanceof String) {
            obj.setClientToken((String)member.getValue());
          }
          break;
        case "lease_duration":
          if (member.getValue() instanceof Number) {
            obj.setLeaseDuration(((Number)member.getValue()).longValue());
          }
          break;
        case "metadata":
          if (member.getValue() instanceof JsonObject) {
            obj.setMetadata(((JsonObject)member.getValue()).copy());
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
        case "renewable":
          if (member.getValue() instanceof Boolean) {
            obj.setRenewable((Boolean)member.getValue());
          }
          break;
        case "user_id":
          break;
        case "username":
          break;
      }
    }
  }

  public static void toJson(Auth obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(Auth obj, java.util.Map<String, Object> json) {
    if (obj.getAccessor() != null) {
      json.put("accessor", obj.getAccessor());
    }
    if (obj.getApplicationId() != null) {
      json.put("application_id", obj.getApplicationId());
    }
    if (obj.getClientToken() != null) {
      json.put("client_token", obj.getClientToken());
    }
    json.put("lease_duration", obj.getLeaseDuration());
    if (obj.getMetadata() != null) {
      json.put("metadata", obj.getMetadata());
    }
    if (obj.getPolicies() != null) {
      JsonArray array = new JsonArray();
      obj.getPolicies().forEach(item -> array.add(item));
      json.put("policies", array);
    }
    json.put("renewable", obj.isRenewable());
    if (obj.getUserId() != null) {
      json.put("user_id", obj.getUserId());
    }
    if (obj.getUsername() != null) {
      json.put("username", obj.getUsername());
    }
  }
}

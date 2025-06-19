package io.vertx.config.vault.client;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.impl.JsonUtil;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Converter and mapper for {@link io.vertx.config.vault.client.Secret}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.config.vault.client.Secret} original class using Vert.x codegen.
 */
public class SecretConverter {


  private static final Base64.Decoder BASE64_DECODER = JsonUtil.BASE64_DECODER;
  private static final Base64.Encoder BASE64_ENCODER = JsonUtil.BASE64_ENCODER;

   static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, Secret obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "data":
          if (member.getValue() instanceof JsonObject) {
            obj.setData(((JsonObject)member.getValue()).copy());
          }
          break;
        case "lease_duration":
          if (member.getValue() instanceof Number) {
            obj.setLeaseDuration(((Number)member.getValue()).longValue());
          }
          break;
        case "lease_id":
          if (member.getValue() instanceof String) {
            obj.setLeaseId((String)member.getValue());
          }
          break;
        case "renewable":
          if (member.getValue() instanceof Boolean) {
            obj.setRenewable((Boolean)member.getValue());
          }
          break;
        case "request_id":
          if (member.getValue() instanceof String) {
            obj.setRequestId((String)member.getValue());
          }
          break;
      }
    }
  }

   static void toJson(Secret obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

   static void toJson(Secret obj, java.util.Map<String, Object> json) {
    if (obj.getData() != null) {
      json.put("data", obj.getData());
    }
    json.put("lease_duration", obj.getLeaseDuration());
    if (obj.getLeaseId() != null) {
      json.put("lease_id", obj.getLeaseId());
    }
    json.put("renewable", obj.isRenewable());
    if (obj.getRequestId() != null) {
      json.put("request_id", obj.getRequestId());
    }
  }
}

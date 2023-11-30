package io.vertx.config.vault.client;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.impl.JsonUtil;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * Converter and mapper for {@link io.vertx.config.vault.client.Lookup}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.config.vault.client.Lookup} original class using Vert.x codegen.
 */
public class LookupConverter {


  private static final Base64.Decoder BASE64_DECODER = JsonUtil.BASE64_DECODER;
  private static final Base64.Encoder BASE64_ENCODER = JsonUtil.BASE64_ENCODER;

   static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, Lookup obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "accessor":
          if (member.getValue() instanceof String) {
            obj.setAccessor((String)member.getValue());
          }
          break;
        case "creation_time":
          if (member.getValue() instanceof Number) {
            obj.setCreationTime(((Number)member.getValue()).longValue());
          }
          break;
        case "creation_ttl":
          if (member.getValue() instanceof Number) {
            obj.setCreationTTL(((Number)member.getValue()).longValue());
          }
          break;
        case "display_name":
          if (member.getValue() instanceof String) {
            obj.setDisplayName((String)member.getValue());
          }
          break;
        case "explicit_max_ttl":
          if (member.getValue() instanceof Number) {
            obj.setExplicitMaxTTL(((Number)member.getValue()).longValue());
          }
          break;
        case "id":
          if (member.getValue() instanceof String) {
            obj.setId((String)member.getValue());
          }
          break;
        case "last_renewal_time":
          if (member.getValue() instanceof Number) {
            obj.setLastRenewalTime(((Number)member.getValue()).longValue());
          }
          break;
        case "num_uses":
          if (member.getValue() instanceof Number) {
            obj.setNumUses(((Number)member.getValue()).longValue());
          }
          break;
        case "orphan":
          if (member.getValue() instanceof Boolean) {
            obj.setOrphan((Boolean)member.getValue());
          }
          break;
        case "path":
          if (member.getValue() instanceof String) {
            obj.setPath((String)member.getValue());
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
        case "ttl":
          if (member.getValue() instanceof Number) {
            obj.setTtl(((Number)member.getValue()).longValue());
          }
          break;
        case "renewable":
          if (member.getValue() instanceof Boolean) {
            obj.setRenewable((Boolean)member.getValue());
          }
          break;
        case "metadata":
          if (member.getValue() instanceof JsonObject) {
            obj.setMetadata(((JsonObject)member.getValue()).copy());
          }
          break;
      }
    }
  }

   static void toJson(Lookup obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

   static void toJson(Lookup obj, java.util.Map<String, Object> json) {
    if (obj.getAccessor() != null) {
      json.put("accessor", obj.getAccessor());
    }
    json.put("creation_time", obj.getCreationTime());
    json.put("creation_ttl", obj.getCreationTTL());
    if (obj.getDisplayName() != null) {
      json.put("display_name", obj.getDisplayName());
    }
    json.put("explicit_max_ttl", obj.getExplicitMaxTTL());
    if (obj.getId() != null) {
      json.put("id", obj.getId());
    }
    json.put("last_renewal_time", obj.getLastRenewalTime());
    json.put("num_uses", obj.getNumUses());
    json.put("orphan", obj.isOrphan());
    if (obj.getPath() != null) {
      json.put("path", obj.getPath());
    }
    if (obj.getPolicies() != null) {
      JsonArray array = new JsonArray();
      obj.getPolicies().forEach(item -> array.add(item));
      json.put("policies", array);
    }
    json.put("ttl", obj.getTtl());
    json.put("renewable", obj.isRenewable());
    if (obj.getMetadata() != null) {
      json.put("metadata", obj.getMetadata());
    }
  }
}

package io.vertx.config;

import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.impl.JsonUtil;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

/**
 * Converter and mapper for {@link io.vertx.config.ConfigRetrieverOptions}.
 * NOTE: This class has been automatically generated from the {@link io.vertx.config.ConfigRetrieverOptions} original class using Vert.x codegen.
 */
public class ConfigRetrieverOptionsConverter {


  public static void fromJson(Iterable<java.util.Map.Entry<String, Object>> json, ConfigRetrieverOptions obj) {
    for (java.util.Map.Entry<String, Object> member : json) {
      switch (member.getKey()) {
        case "includeDefaultStores":
          if (member.getValue() instanceof Boolean) {
            obj.setIncludeDefaultStores((Boolean)member.getValue());
          }
          break;
        case "scanPeriod":
          if (member.getValue() instanceof Number) {
            obj.setScanPeriod(((Number)member.getValue()).longValue());
          }
          break;
        case "stores":
          if (member.getValue() instanceof JsonArray) {
            java.util.ArrayList<io.vertx.config.ConfigStoreOptions> list =  new java.util.ArrayList<>();
            ((Iterable<Object>)member.getValue()).forEach( item -> {
              if (item instanceof JsonObject)
                list.add(new io.vertx.config.ConfigStoreOptions((io.vertx.core.json.JsonObject)item));
            });
            obj.setStores(list);
          }
          break;
      }
    }
  }

  public static void toJson(ConfigRetrieverOptions obj, JsonObject json) {
    toJson(obj, json.getMap());
  }

  public static void toJson(ConfigRetrieverOptions obj, java.util.Map<String, Object> json) {
    json.put("includeDefaultStores", obj.isIncludeDefaultStores());
    json.put("scanPeriod", obj.getScanPeriod());
    if (obj.getStores() != null) {
      JsonArray array = new JsonArray();
      obj.getStores().forEach(item -> array.add(item.toJson()));
      json.put("stores", array);
    }
  }
}

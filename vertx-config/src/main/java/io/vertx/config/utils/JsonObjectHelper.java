package io.vertx.config.utils;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Objects;
import java.util.Properties;

/**
 * Some utility methods to create json objects from a set of String.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class JsonObjectHelper {

  public static Buffer toBuffer(JsonObject json) {
    return Buffer.buffer(json.encode());
  }

  public static void put(JsonObject json, String name, String value) {
    Objects.requireNonNull(value);

    Boolean bool = asBoolean(value);
    if (bool != null) {
      json.put(name, bool);
      return;
    }

    Double integer = asNumber(value);
    if (integer != null) {
      json.put(name, integer);
      return;
    }

    JsonObject obj = asJsonObject(value);
    if (obj != null) {
      json.put(name, obj);
      return;
    }

    JsonArray arr = asJsonArray(value);
    if (arr != null) {
      json.put(name, arr);
      return;
    }

    json.put(name, value);
  }

  public  static Object convert(String value) {
    Objects.requireNonNull(value);

    Boolean bool = asBoolean(value);
    if (bool != null) {
      return bool;
    }

    Double integer = asNumber(value);
    if (integer != null) {
      return integer;
    }

    JsonObject obj = asJsonObject(value);
    if (obj != null) {
      return obj;
    }

    JsonArray arr = asJsonArray(value);
    if (arr != null) {
      return arr;
    }

    return value;
  }

  private static Double asNumber(String s) {
    try {
      return Double.parseDouble(s);
    } catch (NumberFormatException nfe) {
      return null;
    }
  }

  private static Boolean asBoolean(String s) {
    if (s.equalsIgnoreCase("true")) {
      return Boolean.TRUE;
    } else if (s.equalsIgnoreCase("false")) {
      return Boolean.FALSE;
    } else {
      return null;
    }
  }

  private static JsonObject asJsonObject(String s) {
    if (s.startsWith("{") && s.endsWith("}")) {
      try {
        return new JsonObject(s);
      } catch (Exception e) {
        return null;
      }
    }
    return null;
  }

  private static JsonArray asJsonArray(String s) {
    if (s.startsWith("[") && s.endsWith("]")) {
      try {
        return new JsonArray(s);
      } catch (Exception e) {
        return null;
      }
    }
    return null;
  }

  public static JsonObject from(Properties props) {
    JsonObject json = new JsonObject();
    props.stringPropertyNames()
      .forEach(name -> put(json, name, props.getProperty(name)));
    return json;
  }
}

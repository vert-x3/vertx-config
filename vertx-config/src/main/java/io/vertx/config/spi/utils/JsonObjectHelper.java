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

package io.vertx.config.spi.utils;

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

  /**
   * Deprecated. {@link JsonObject} now has a {@link JsonObject#toBuffer()} method.
   *
   * @deprecated use {@link JsonObject#toBuffer()} instead
   */
  @Deprecated
  public static Buffer toBuffer(JsonObject json) {
    return json.toBuffer();
  }

  public static void put(JsonObject json, String name, String value, boolean rawData) {
    json.put(name, rawData ? value : convert(value));
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
      .forEach(name -> put(json, name, props.getProperty(name), false));
    return json;
  }
}

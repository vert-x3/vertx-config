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

import io.vertx.config.spi.ConfigProcessor;
import io.vertx.config.spi.ConfigStoreFactory;

import java.util.HashMap;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * A class to deal with configuration prcessors.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Processors {

  private static final HashMap<String, ConfigProcessor> PROCESSORS = new HashMap<>();

  static {
    synchronized (Processors.class) {
      ServiceLoader<ConfigProcessor> processorImpl =
          ServiceLoader.load(ConfigProcessor.class,
              ConfigStoreFactory.class.getClassLoader());
      processorImpl.iterator().forEachRemaining(processor -> PROCESSORS.put(processor.name(), processor));
    }
  }

  /**
   * Gets a configuration processor matching with the given format.
   *
   * @param format the format, must not be {@code null}
   * @return the configuration processor or {@code null} if none matches
   */
  public static ConfigProcessor get(String format) {
    synchronized (Processors.class) {
      return PROCESSORS.get(format);
    }
  }

  /**
   * @return the set of supported formats
   */
  public static Set<String> getSupportedFormats() {
    synchronized (Processors.class) {
      return PROCESSORS.keySet();
    }
  }
}

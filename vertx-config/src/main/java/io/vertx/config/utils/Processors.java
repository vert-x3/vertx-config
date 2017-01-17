package io.vertx.config.utils;

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

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

package io.vertx.config.impl;


import io.vertx.config.ConfigChange;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.config.spi.ConfigProcessor;
import io.vertx.config.spi.ConfigStore;
import io.vertx.config.spi.ConfigStoreFactory;
import io.vertx.config.spi.utils.Processors;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.streams.ReadStream;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link ConfigRetriever}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ConfigRetrieverImpl implements ConfigRetriever {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigRetrieverImpl.class);

  private static final String DEFAULT_CONFIG_PATH = "conf" + File.separator + "config.json";

  private final Vertx vertx;
  private final List<ConfigurationProvider> providers;
  private long scan;
  private final List<Handler<ConfigChange>> listeners = new ArrayList<>();
  private final ConfigStreamImpl streamOfConfiguration = new ConfigStreamImpl();
  private final ConfigRetrieverOptions options;

  private JsonObject current = new JsonObject();

  private Handler<Void> beforeScan;
  private Function<JsonObject, JsonObject> processor;

  public ConfigRetrieverImpl(Vertx vertx, ConfigRetrieverOptions options) {
    this.vertx = vertx;
    this.options = options;

    ServiceLoader<ConfigStoreFactory> storeImpl =
        ServiceLoader.load(ConfigStoreFactory.class,
            ConfigStoreFactory.class.getClassLoader());

    Map<String, ConfigStoreFactory> nameToImplMap = new HashMap<>();
    storeImpl.iterator().forEachRemaining(factory -> nameToImplMap.put(factory.name(), factory));
    if (nameToImplMap.isEmpty()) {
      throw new IllegalStateException("No configuration store implementations found on the classpath");
    }

    List<ConfigStoreOptions> stores = options.getStores();
    if (options.isIncludeDefaultStores()) {
      stores = new ArrayList<>();
      stores.add(
        new ConfigStoreOptions().setType("json")
          .setConfig(vertx.getOrCreateContext().config()));
      stores.add(new ConfigStoreOptions().setType("sys"));
      stores.add(new ConfigStoreOptions().setType("env"));

      // Insert the default config if configured.
      String defaultConfigPath = getDefaultConfigPath();
      if (defaultConfigPath != null  && ! defaultConfigPath.trim().isEmpty()) {
        String format = extractFormatFromFileExtension(defaultConfigPath);
        LOGGER.info("Config file path: " + defaultConfigPath + ", format:" + format);
        stores.add(new ConfigStoreOptions()
          .setType("file").setFormat(format)
          .setOptional(true)
          .setConfig(new JsonObject().put("path", defaultConfigPath)));
      }
      stores.addAll(options.getStores());
    }

    // Iterate over the configured `stores` to configuration the stores
    providers = new ArrayList<>();
    for (ConfigStoreOptions option : stores) {
      String type = option.getType();
      if (type == null) {
        throw new IllegalArgumentException(
            "the `type` entry is mandatory in a configuration store configuration");
      }

      ConfigStoreFactory factory = nameToImplMap.get(type);
      if (factory == null) {
        throw new IllegalArgumentException("unknown configuration store implementation: " +
            type + " (known implementations are: " + nameToImplMap.keySet() + ")");
      }

      JsonObject config = option.getConfig();
      if (config == null) {
        config = new JsonObject();
      }
      ConfigStore store = factory.create(vertx, config);

      String format = option.getFormat() != null ? option.getFormat() : "json";
      ConfigProcessor processor = Processors.get(format);
      if (processor == null) {
        throw new IllegalArgumentException("unknown configuration format: " + format + " (supported formats are: " +
            Processors.getSupportedFormats());
      }
      providers.add(new ConfigurationProvider(store, processor, option.getConfig(), option.isOptional()));
    }
  }

  static String extractFormatFromFileExtension(String path) {
    int index = path.lastIndexOf(".");
    if (index == -1) {
      // Default format
      return "json";
    } else {
      String ext = path.substring(index + 1);
      if (ext.trim().isEmpty()) {
        return "json";
      }

      if ("yml".equalsIgnoreCase(ext)) {
        ext = "yaml";
      }
      return ext.toLowerCase();
    }
  }

  private String getDefaultConfigPath() {
    String value = System.getenv("VERTX_CONFIG_PATH");
    if (value == null  || value.trim().isEmpty()) {
      value = System.getProperty("vertx-config-path");
    }
    if (value != null  && ! value.trim().isEmpty()) {
      return value.trim();
    }
    boolean exists = vertx.fileSystem().existsBlocking(DEFAULT_CONFIG_PATH);
    if (exists) {
      return DEFAULT_CONFIG_PATH;
    }
    return null;
  }

  public synchronized void initializePeriodicScan() {
    if (options.getScanPeriod() > 0) {
      this.scan = vertx.setPeriodic(options.getScanPeriod(), l -> scan());
    } else {
      this.scan = -1;
    }
  }

  @Override
  public void getConfig(Handler<AsyncResult<JsonObject>> completionHandler) {
    Objects.requireNonNull(completionHandler);
    compute(ar -> {
      if (ar.succeeded()) {
        synchronized ((ConfigRetrieverImpl.this)) {
          current = ar.result();
          streamOfConfiguration.handle(current);
        }
      }
      completionHandler.handle(ar);
    });
  }

  @Override
  public synchronized void close() {
    if (scan != -1) {
      vertx.cancelTimer(scan);
    }

    streamOfConfiguration.close();

    for (ConfigurationProvider provider : providers) {
      provider.close(v -> {
      });
    }
  }

  @Override
  public synchronized JsonObject getCachedConfig() {
    return current.copy();
  }

  @Override
  public void listen(Handler<ConfigChange> listener) {
    Objects.requireNonNull(listener);
    listeners.add(listener);
  }

  @Override
  public ConfigRetriever setBeforeScanHandler(Handler<Void> handler) {
    this.beforeScan = Objects.requireNonNull(handler, "The handler must not be `null`");
    return this;
  }

  @Override
  public ConfigRetriever setConfigurationProcessor(Function<JsonObject, JsonObject> processor) {
    this.processor = Objects.requireNonNull(processor, "The processor must not be `null`");
    return this;
  }

  @Override
  public ReadStream<JsonObject> configStream() {
    return streamOfConfiguration;
  }

  private void scan() {
    if (beforeScan != null) {
      beforeScan.handle(null);
    }
    compute(ar -> {
      if (ar.failed()) {
        streamOfConfiguration.fail(ar.cause());
        LOGGER.error("Error while scanning configuration", ar.cause());
      } else {
        synchronized (ConfigRetrieverImpl.this) {
          // Check for changes
          if (!current.equals(ar.result())) {
            JsonObject prev = current;
            current = ar.result();

            listeners.forEach(l -> l.handle(new ConfigChange(prev, current)));
            try {
              streamOfConfiguration.handle(current);
            } catch (Throwable e) {
              // Report the error on the context exception handler.
              if (vertx.exceptionHandler() != null) {
                vertx.exceptionHandler().handle(e);
              } else {
                throw e;
              }
            }
          }
        }
      }
    });
  }

  private void compute(Handler<AsyncResult<JsonObject>> completionHandler) {
    List<Future> futures = providers.stream()
        .map(s -> {
          Promise<JsonObject> conf = Promise.promise();
          s.get(vertx, ar -> {
            if (ar.succeeded()) {
              conf.tryComplete(ar.result());
            } else {
              conf.tryFail(ar.cause());
            }
          });
          return conf.future();
        })
        .collect(Collectors.toList());

    CompositeFuture.all(futures).setHandler(r -> {
      if (r.failed()) {
        try {
          completionHandler.handle(Future.failedFuture(r.cause()));
        } catch (Throwable e) {
          // Report the error on the context exception handler.
          if (vertx.exceptionHandler() != null) {
            vertx.exceptionHandler().handle(e);
          } else {
            throw e;
          }
        }
      } else {
        // Merge the different futures
        JsonObject json = new JsonObject();
        futures.forEach(future -> json.mergeIn((JsonObject) future.result(), true));
        try {
          JsonObject computed = json;
          if (processor != null) {
            processConfigurationAndReport(completionHandler, json);
          } else {
            completionHandler.handle(Future.succeededFuture(computed));
          }
        } catch (Throwable e) {
          // Report the error on the context exception handler.
          if (vertx.exceptionHandler() != null) {
            vertx.exceptionHandler().handle(e);
          } else {
            throw e;
          }
        }
      }
    });
  }

  private void processConfigurationAndReport(Handler<AsyncResult<JsonObject>> completionHandler, JsonObject json) {
    JsonObject computed;
    try {
      computed = processor.apply(json);
      completionHandler.handle(Future.succeededFuture(computed));
    } catch (Throwable e) {
      completionHandler.handle(Future.failedFuture(e));
    }
  }

  /**
   * @return the list of providers. For introspection purpose.
   */
  public List<ConfigurationProvider> getProviders() {
      return Collections.unmodifiableList(providers);
  }

  private class ConfigStreamImpl implements ReadStream<JsonObject> {

    private Handler<JsonObject> handler;
    private Handler<Throwable> exceptionHandler;
    private Handler<Void> endHandler;

    private JsonObject last;
    private long demand = Long.MAX_VALUE;

    @Override
    public synchronized ReadStream<JsonObject> exceptionHandler(Handler<Throwable> handler) {
      Objects.requireNonNull(handler);
      this.exceptionHandler = handler;
      return this;
    }

    @Override
    public ReadStream<JsonObject> handler(Handler<JsonObject> handler) {
      Objects.requireNonNull(handler);
      JsonObject conf;
      synchronized (this) {
        this.handler = handler;
        conf = getCachedConfig();
      }

      if (conf != null && !conf.isEmpty()) {
        vertx.runOnContext(v -> this.handler.handle(conf));
      }

      return this;
    }

    @Override
    public synchronized ReadStream<JsonObject> pause() {
      demand = 0L;
      return this;
    }

    @Override
    public synchronized ReadStream<JsonObject> resume() {
      boolean check = demand == 0;
      demand = Long.MAX_VALUE;
      if (check) {
        checkPending();
      }
      return this;
    }

    @Override
    public synchronized ReadStream<JsonObject> fetch(long amount) {
      boolean check = demand == 0;
      demand += amount;
      if (demand < 0L) {
        demand = Long.MAX_VALUE;
      }
      if (check) {
        checkPending();
      }
      return this;
    }

    private void checkPending() {
      Handler<JsonObject> succ = handler;
      JsonObject conf = last;
      last = null;
      if (conf != null) {
        if (demand != Long.MAX_VALUE) {
          demand--;
        }
        if (succ != null) {
          vertx.runOnContext(v -> succ.handle(conf));
        }
      }
    }

    @Override
    public synchronized ReadStream<JsonObject> endHandler(Handler<Void> endHandler) {
      Objects.requireNonNull(endHandler);
      this.endHandler = endHandler;
      return this;
    }

    synchronized void handle(JsonObject conf) {
      Handler<JsonObject> succ = handler;
      boolean isPaused = demand == 0;
      if (isPaused) {
        last = conf;
      } else if (demand < Long.MAX_VALUE) {
        demand--;
      }

      if (!isPaused && succ != null) {
        vertx.runOnContext(v -> succ.handle(conf));
      }

    }

    void fail(Throwable cause) {
      Handler<Throwable> err;
      synchronized (this) {
        err = exceptionHandler;
      }

      if (err != null) {
        vertx.runOnContext(v -> err.handle(cause));
      }

    }

    void close() {
      Handler<Void> handler;
      synchronized (this) {
        handler = endHandler;
      }
      if (handler != null) {
        vertx.runOnContext(v -> handler.handle(null));
      }
    }
  }
}

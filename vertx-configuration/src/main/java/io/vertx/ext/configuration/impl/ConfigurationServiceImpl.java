package io.vertx.ext.configuration.impl;


import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.configuration.ConfigurationService;
import io.vertx.ext.configuration.ConfigurationServiceOptions;
import io.vertx.ext.configuration.ConfigurationStoreOptions;
import io.vertx.ext.configuration.spi.ConfigurationProcessor;
import io.vertx.ext.configuration.spi.ConfigurationStore;
import io.vertx.ext.configuration.spi.ConfigurationStoreFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link ConfigurationService}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ConfigurationServiceImpl implements ConfigurationService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationServiceImpl.class);

  private final Vertx vertx;
  private final List<ConfigurationProvider> providers;
  private final long scan;
  private final String broadcast;
  private final List<Handler<JsonObject>> listeners = new ArrayList<>();

  private long lastRetrieval = System.currentTimeMillis();
  private JsonObject current = new JsonObject();

  public ConfigurationServiceImpl(Vertx vertx, ConfigurationServiceOptions options) {
    this.vertx = vertx;

    ServiceLoader<ConfigurationStoreFactory> storeImpl =
        ServiceLoader.load(ConfigurationStoreFactory.class,
            ConfigurationStoreFactory.class.getClassLoader());

    ServiceLoader<ConfigurationProcessor> processorImpl =
        ServiceLoader.load(ConfigurationProcessor.class,
            ConfigurationStoreFactory.class.getClassLoader());

    Map<String, ConfigurationStoreFactory> nameToImplMap = new HashMap<>();
    storeImpl.iterator().forEachRemaining(factory -> nameToImplMap.put(factory.name(), factory));
    if (nameToImplMap.isEmpty()) {
      throw new IllegalStateException("No configuration store implementations found on the classpath");
    }

    Map<String, ConfigurationProcessor> processors = new HashMap<>();
    processorImpl.iterator().forEachRemaining(processor -> processors.put(processor.name(), processor));

    // Iterate over the configured `stores` to configuration the stores
    providers = new ArrayList<>();
    for (ConfigurationStoreOptions option : options.getStores()) {
      String type = option.getType();
      if (type == null) {
        throw new IllegalArgumentException(
            "the `type` entry is mandatory in a configuration store configuration");
      }

      ConfigurationStoreFactory factory = nameToImplMap.get(type);
      if (factory == null) {
        throw new IllegalArgumentException("unknown configuration store implementation: " +
            type + " (known implementations are: " + nameToImplMap.keySet() + ")");
      }

      JsonObject config = option.getConfig();
      if (config == null) {
        config = new JsonObject();
      }
      ConfigurationStore store = factory.create(vertx, config);

      String format = option.getFormat() != null ? option.getFormat() : "json";
      ConfigurationProcessor processor = processors.get(format);
      if (processor == null) {
        throw new IllegalArgumentException("unknown configuration format: " + format + " (supported formats are: " +
            processors.keySet());
      }
      providers.add(new ConfigurationProvider(store, processor));
    }

    this.broadcast = options.getBroadcastAddress();

    if (options.getScanPeriod() > 0) {
      this.scan = vertx.setPeriodic(options.getScanPeriod(), l -> scan());
    } else {
      this.scan = -1;
    }

    getConfiguration(ar -> {
      if (ar.succeeded()) {
        synchronized (ConfigurationServiceImpl.this) {
          broadcast(current, lastRetrieval);
        }
      }
    });
  }

  @Override
  public void getConfiguration(Handler<AsyncResult<JsonObject>> completionHandler) {
    Objects.requireNonNull(completionHandler);
    compute(ar -> {
      if (ar.succeeded()) {
        synchronized ((ConfigurationServiceImpl.this)) {
          current = ar.result();
          lastRetrieval = System.currentTimeMillis();
        }
      }
      completionHandler.handle(ar);
    });
  }

  @Override
  public void close() {
    if (scan != -1) {
      vertx.cancelTimer(scan);
    }
  }

  @Override
  public synchronized JsonObject getCachedConfiguration() {
    return current.copy();
  }

  @Override
  public synchronized void listen(Handler<JsonObject> listener) {
    Objects.requireNonNull(listener);
    listeners.add(listener);
  }


  private void scan() {
    compute(ar -> {
      if (ar.failed()) {
        LOGGER.error("Error while scanning configuration", ar.cause());
      } else {
        synchronized (ConfigurationServiceImpl.this) {
          // Check for changes
          if (!current.equals(ar.result())) {
            lastRetrieval = System.currentTimeMillis();
            current = ar.result();
            listeners.stream().forEach(l -> l.handle(current));
            broadcast(current, lastRetrieval);
          }
        }
      }
    });
  }

  private void broadcast(JsonObject config, long time) {
    if (broadcast != null) {
      vertx.eventBus().publish(broadcast, config,
          new DeliveryOptions().addHeader("timestamp", Long.toString(time)));
    }
  }

  private void compute(Handler<AsyncResult<JsonObject>> completionHandler) {
    List<Future> futures = providers.stream()
        .map(s -> {
          Future<JsonObject> conf = Future.future();
          s.get(vertx, conf.completer());
          return conf;
        })
        .collect(Collectors.toList());

    CompositeFuture.all(futures).setHandler(r -> {
      if (r.failed()) {
        completionHandler.handle(Future.failedFuture(r.cause()));
      } else {
        // Merge the different futures
        JsonObject json = new JsonObject();
        futures.stream().forEach(future -> json.mergeIn((JsonObject) future.result()));
        completionHandler.handle(Future.succeededFuture(json));
      }
    });
  }
}

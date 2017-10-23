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
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.streams.ReadStream;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link ConfigRetriever}.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ConfigRetrieverImpl implements ConfigRetriever {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigRetrieverImpl.class);

  private final Vertx vertx;
  private final List<ConfigurationProvider> providers;
  private long scan;
  private final List<Handler<ConfigChange>> listeners = new ArrayList<>();
  private final ConfigStreamImpl streamOfConfiguration = new ConfigStreamImpl();
  private final ConfigRetrieverOptions options;

  private JsonObject current = new JsonObject();

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

    // Iterate over the configured `stores` to configuration the stores
    providers = new ArrayList<>();
    for (ConfigStoreOptions option : options.getStores()) {
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
      providers.add(new ConfigurationProvider(store, processor, option.getConfig()));
    }
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
  public ReadStream<JsonObject> configStream() {
    return streamOfConfiguration;
  }

  private void scan() {
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
          Future<JsonObject> conf = Future.future();
          s.get(vertx, ar -> {
            if (ar.succeeded()) {
              conf.tryComplete(ar.result());
            } else {
              conf.tryFail(ar.cause());
            }
          });
          return conf;
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
          completionHandler.handle(Future.succeededFuture(json));
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
    private boolean paused = false;

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
      paused = true;
      return this;
    }

    @Override
    public ReadStream<JsonObject> resume() {
      JsonObject conf;
      Handler<JsonObject> succ;
      synchronized (this) {
        if (! paused) {
          // Cannot resume a non paused stream
          return this;
        }

        paused = false;
        conf = last;
        if (last != null) {
          last = null;
        }
        succ = this.handler;
      }

      if (conf != null && succ != null) {
        vertx.runOnContext(v -> succ.handle(conf));
      }

      return this;
    }

    @Override
    public synchronized ReadStream<JsonObject> endHandler(Handler<Void> endHandler) {
      Objects.requireNonNull(endHandler);
      this.endHandler = endHandler;
      return this;
    }

    void handle(JsonObject conf) {
      Handler<JsonObject> succ;
      boolean isPaused;
      synchronized (this) {
        succ = handler;
        isPaused = paused;
        if (paused) {
          last = conf;
        }
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

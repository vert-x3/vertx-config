package io.vertx.config.kubernetes;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.*;
import io.vertx.config.utils.JsonObjectHelper;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.config.spi.ConfigStore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * An implementation of configuration store reading config map from Kubernetes.
 */
public class ConfigMapStore implements ConfigStore {
  private static final String KUBERNETES_NAMESPACE = System.getenv("KUBERNETES_NAMESPACE");
  private final Vertx vertx;
  private final JsonObject configuration;
  private final String namespace;
  private final String name;
  private final String key;
  private final boolean secret;
  private KubernetesClient client;


  public ConfigMapStore(Vertx vertx, JsonObject configuration) {
    this.vertx = vertx;
    this.configuration = configuration;
    String ns = configuration.getString("namespace");
    if (ns == null) {
      if (KUBERNETES_NAMESPACE != null) {
        ns = KUBERNETES_NAMESPACE;
      } else {
        ns = "default";
      }
    }
    this.namespace = ns;
    this.name = configuration.getString("name");
    this.key = configuration.getString("key");
    this.secret = configuration.getBoolean("secret", false);
    Objects.requireNonNull(this.name);
  }

  /**
   * For testing purpose only - inject the kubernetes client.
   *
   * @param client the client.
   */
  synchronized void setClient(KubernetesClient client) {
    this.client = client;
  }

  @Override
  public synchronized void close(Handler<Void> completionHandler) {
    if (client != null) {
      client.close();
      client = null;
    }
  }

  private Future<KubernetesClient> getClient() {
    Future<KubernetesClient> result = Future.future();
    String master = configuration.getString("master", KubernetesUtils.getDefaultKubernetesMasterUrl());
    vertx.<KubernetesClient>executeBlocking(future -> {
      String accountToken = configuration.getString("token");
      if (accountToken == null) {
        accountToken = KubernetesUtils.getTokenFromFile();
      }

      Config config = new ConfigBuilder().withOauthToken(accountToken).withMasterUrl(master).withTrustCerts(true)
          .build();

      DefaultKubernetesClient kubernetesClient;
      try {
        kubernetesClient = new DefaultKubernetesClient(config);
        future.complete(kubernetesClient);
      } catch (KubernetesClientException e) {
        future.fail(e);
      }
    }, ar -> {
      if (ar.failed()) {
        result.fail(ar.cause());
      } else {
        this.client = ar.result();
        result.complete(ar.result());
      }
    });
    return result;
  }

  @Override
  public synchronized void get(Handler<AsyncResult<Buffer>> completionHandler) {
    Future<KubernetesClient> retrieveClient;
    if (client == null) {
      retrieveClient = getClient();
    } else {
      retrieveClient = Future.succeededFuture(client);
    }

    retrieveClient.compose(client -> {
      Future<Buffer> json = Future.future();
      vertx.executeBlocking(
          future -> {
            if (secret) {
              Secret secret = client.secrets().inNamespace(namespace).withName(name).get();
              if (secret == null) {
                future.fail("Cannot find the config map '" + name + "' in '" + namespace + "'");
              } else {
                if (this.key == null) {
                  Map<String, Object> cm = asObjectMap(secret.getData());
                  future.complete(Buffer.buffer(new JsonObject(cm).encode()));
                } else {
                  String value = secret.getData().get(this.key);
                  if (value == null) {
                    future.fail("cannot find key '" + this.key + "' in the secret '" + this.name + "'");
                  } else {
                    future.complete(Buffer.buffer(value));
                  }
                }
              }
            } else {
              ConfigMap map = client.configMaps().inNamespace(namespace).withName(name).get();
              if (map == null) {
                future.fail("Cannot find the config map '" + name + "' in '" + namespace + "'");
              } else {
                if (this.key == null) {
                  Map<String, Object> cm = asObjectMap(map.getData());
                  future.complete(Buffer.buffer(new JsonObject(cm).encode()));
                } else {
                  String value = map.getData().get(this.key);
                  if (value == null) {
                    future.fail("cannot find key '" + this.key + "' in the config map '" + this.name + "'");
                  } else {
                    future.complete(Buffer.buffer(value));
                  }
                }
              }
            }
          },
          json.completer()
      );
      return json;
    }).setHandler(completionHandler);
  }

  private static Map<String, Object> asObjectMap(Map<String, String> source) {
    if (source == null) {
      return new HashMap<>();
    }
    return source.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
        entry -> JsonObjectHelper.convert(entry.getValue())));
  }

}

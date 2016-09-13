package io.vertx.ext.configuration.kubernetes;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapList;
import io.fabric8.kubernetes.client.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.configuration.spi.ConfigurationStore;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * An implementation of configuration store reading config map from Kubernetes.
 */
public class ConfigMapStore implements ConfigurationStore {
	private final Vertx vertx;
	private final JsonObject configuration;
	private String namespace;
	private final String name;
	private final String key;
	private KubernetesClient client;

	public ConfigMapStore(Vertx vertx, JsonObject configuration) {
		this.vertx = vertx;
		this.configuration = configuration;
		this.namespace = configuration.getString("namespace");
		this.name = configuration.getString("name");
		this.key = configuration.getString("key");
		Objects.requireNonNull(this.name);
	}

	@Override
	public void close(Handler<Void> completionHandler) {
		if (client != null) {
			client.close();
			client = null;
		}
	}

	private Future<KubernetesClient> getClient() {
		Future<KubernetesClient> result = Future.future();
		namespace = System.getenv().get("KUBERNETES_NAMESPACE") != null ? System.getenv().get("KUBERNETES_NAMESPACE") : "default";
		String master = configuration.getString("master", KubernetesUtils.getDefaultKubernetesMasterUrl());
		vertx.<KubernetesClient>executeBlocking(future -> {
			String accountToken = configuration.getString("token");
			if (accountToken == null) {
				accountToken = KubernetesUtils.getTokenFromFile();
			}

			Config config = new ConfigBuilder().withOauthToken(accountToken).withMasterUrl(master).withTrustCerts(true)
					.build();

			DefaultKubernetesClient kubernetesClient = null;
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
	public void get(Handler<AsyncResult<Buffer>> completionHandler) {
		Future<KubernetesClient> retrieveClient;
		if (client == null) {
			retrieveClient = getClient();
		} else {
			retrieveClient = Future.succeededFuture(client);
		}

		retrieveClient.compose(client -> {
			Future<Buffer> json = Future.future();
			ConfigMapList list = client.configMaps().inNamespace(namespace).list();
			List<ConfigMap> maps = list.getItems();
			for (ConfigMap map : maps) {
				String name = map.getMetadata().getName();
				if (this.name.equalsIgnoreCase(name)) {
          if (this.key == null) {
						Map<String, Object> cm = asObjectMap(map.getData());
						json.complete(Buffer.buffer(new JsonObject(cm).encode()));
					} else {
						String value = map.getData().get(this.key);
						if (value == null) {
							json.fail("cannot find key '" + this.key + "' in the config map '" + this.name + "'");
						} else {
							json.complete(Buffer.buffer(value));
						}
					}
					return json;
				}
			}
			
			json.fail("Cannot find the config map '" + name + "' in '" + namespace + "'");
			return json;
		}).setHandler(ar -> {
			if (ar.failed()) {
				completionHandler.handle(Future.failedFuture(ar.cause()));
			} else {
				completionHandler.handle(Future.succeededFuture(ar.result()));
			}
		});
	}

	private static Map<String, Object> asObjectMap(Map<String, String> source) {
		return source.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

}

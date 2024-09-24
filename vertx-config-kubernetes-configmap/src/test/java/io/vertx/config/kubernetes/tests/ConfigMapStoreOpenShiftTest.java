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

package io.vertx.config.kubernetes.tests;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.openshift.client.server.mock.OpenShiftMockServer;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.runner.RunWith;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class ConfigMapStoreOpenShiftTest extends ConfigMapStoreTest {

  @Before
  public void setUp(TestContext tc) throws MalformedURLException {
    vertx = Vertx.vertx();
    vertx.exceptionHandler(tc.exceptionHandler());

    ConfigMap map1 = new ConfigMapBuilder().withMetadata(new ObjectMetaBuilder().withName("my-config-map").build())
      .addToData("my-app-json", SOME_JSON)
      .addToData("my-app-props", SOME_PROPS)
      .build();

    Map<String, String> data = new LinkedHashMap<>();
    data.put("key", "value");
    data.put("bool", "true");
    data.put("count", "3");
    ConfigMap map2 = new ConfigMapBuilder().withMetadata(new ObjectMetaBuilder().withName("my-config-map-2").build())
      .withData(data)
      .build();

    ConfigMap map3 = new ConfigMapBuilder().withMetadata(new ObjectMetaBuilder().withName("my-config-map-x").build())
      .addToData("my-app-json", SOME_JSON)
      .build();

    Secret secret = new SecretBuilder().withMetadata(new ObjectMetaBuilder().withName("my-secret").build())
      .addToData("password", Base64.getEncoder().encodeToString("secret".getBytes(UTF_8)))
      .build();

    server = new OpenShiftMockServer(false);

    server.expect().get().withPath("/api/v1/namespaces/default/configmaps").andReturn(200, new
      ConfigMapListBuilder().addToItems(map1, map2).build()).always();
    server.expect().get().withPath("/api/v1/namespaces/my-project/configmaps").andReturn(200, new
      ConfigMapListBuilder().addToItems(map3).build()).always();

    server.expect().get().withPath("/api/v1/namespaces/default/configmaps/my-config-map")
      .andReturn(200, map1).always();
    server.expect().get().withPath("/api/v1/namespaces/default/configmaps/my-config-map-2")
      .andReturn(200, map2).always();

    server.expect().get().withPath("/api/v1/namespaces/my-project/configmaps/my-config-map-x")
      .andReturn(200, map3).always();

    server.expect().get().withPath("/api/v1/namespaces/default/configmaps/my-unknown-config-map")
      .andReturn(500, null).always();
    server.expect().get().withPath("/api/v1/namespaces/default/configmaps/my-unknown-map")
      .andReturn(500, null).always();

    server.expect().get().withPath("/api/v1/namespaces/my-project/secrets/my-secret").andReturn(200, secret)
      .always();

    server.init();
    client = server.createClient();
    port = new URL(client.getConfiguration().getMasterUrl()).getPort();
  }

}

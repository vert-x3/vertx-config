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

package io.vertx.config.vault.tests;

import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.runner.RunWith;

/**
 * Tests the behavior when using the cert backend (TLS certificates).
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class VaultConfigStoreWithCertsTest extends VaultConfigStoreTestBase {

  @Override
  protected void configureVault() {
    process.setupBackendCert();
    assert process.isRunning();
  }

  @Override
  protected JsonObject getRetrieverConfiguration() {

    JsonObject config = new JsonObject();
    config.put("host", process.getHost());
    config.put("port", process.getPort());
    config.put("ssl", true);
    PemKeyCertOptions options = new PemKeyCertOptions()
      .addCertPath("target/vault/config/ssl/client-cert.pem")
      .addKeyPath("target/vault/config/ssl/client-privatekey.pem");
    config.put("pemKeyCertOptions", options.toJson());

    PemTrustOptions trust = new PemTrustOptions()
      .addCertPath("target/vault/config/ssl/cert.pem");
    config.put("pemTrustStoreOptions", trust.toJson());

    JksOptions jks = new JksOptions()
      .setPath("target/vault/config/ssl/truststore.jks");
    config.put("trustStoreOptions", jks.toJson());

    config.put("auth-backend", "cert");

    return config;
  }

  // TODO redo revoked token - with the right token


}

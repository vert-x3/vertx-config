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

package io.vertx.config.vault;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests the behavior when using a token creation (token backend).
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class VaultConfigStoreWithTokenCreationTest extends VaultConfigStoreTestBase {

  @Override
  protected JsonObject getRetrieverConfiguration() {
    return process.getConfiguration().copy()
      .put("auth-backend", "token")
      .put("renew-window", 5000L) // 5s of grace window
      .put("token-request", new JsonObject().put("ttl", "5s")
        .put("token", process.getToken())
      );
  }

  // TODO redo revoked token - with the right token


  @Test
  public void testExpirationOfToken(TestContext tc) throws InterruptedException {
    JsonObject additionalConfig = getRetrieverConfiguration();
    JsonObject config = additionalConfig.copy()
      .put("path", "secret/app/foo").put("key", "nested");

    retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions()
      .addStore(new ConfigStoreOptions().setType("vault")
        .setConfig(config)).setScanPeriod(-1));

    Async async = tc.async();

    // Step 1 - First retrieval, we should still be below the expiration limit
    retriever.getConfig().onComplete(json -> {
      tc.assertTrue(json.succeeded());

      // Step 2 - Wait until expiration
      vertx.executeBlocking(f -> {
        try {
          Thread.sleep(10000L);
        } catch (InterruptedException e) {
          // Ignore it.
        }
        f.complete();
      }).onComplete(x ->
        retriever.getConfig().onComplete(json2 -> {
          tc.assertTrue(json2.succeeded());
          async.complete();
        }));
    });
  }

}

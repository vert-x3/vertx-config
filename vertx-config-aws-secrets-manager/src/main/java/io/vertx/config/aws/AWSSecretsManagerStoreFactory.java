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

package io.vertx.config.aws;


import io.vertx.config.spi.ConfigStore;
import io.vertx.config.spi.ConfigStoreFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Factory to create {@link AWSSecretsManagerStore} instances.
 *
 * @author <a href="http://indiealexh.com">Alexander Haslam</a>
 */
public class AWSSecretsManagerStoreFactory implements ConfigStoreFactory {
  @Override
  public String name() {
    return "aws-secrets-manager";
  }

  @Override
  public ConfigStore create(Vertx vertx, JsonObject configuration) {
    return new AWSSecretsManagerStore(vertx, configuration);
  }
}

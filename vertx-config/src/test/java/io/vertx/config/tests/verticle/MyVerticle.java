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

package io.vertx.config.tests.verticle;

import io.vertx.core.Future;
import io.vertx.core.VerticleBase;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class MyVerticle extends VerticleBase {

  @Override
  public Future<?> start() throws Exception {
    String mark = config().getString("mark");
    vertx.eventBus().send("test.address", mark);
    return super.start();
  }
}

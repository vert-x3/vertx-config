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

package io.vertx.config.spi;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;

/**
 * Defines a configuration store.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public interface ConfigStore {

  /**
   * Closes the configuration store.
   *
   * @param completionHandler handler called when the cleanup has been completed
   * @deprecated implement {@link #close()} instead
   */
  @Deprecated
  default void close(Handler<Void> completionHandler) {
    completionHandler.handle(null);
  }

  /**
   * Retrieves the configuration store in this store.
   *
   * @param completionHandler the handler to pass the configuration
   * @deprecated implement {@link #get()} instead
   */
  @Deprecated
  default void get(Handler<AsyncResult<Buffer>> completionHandler) {
    completionHandler.handle(Future.failedFuture("Deprecated"));
  }

  /**
   * Retrieves the configuration store in this store.
   *
   * @return a {@code Future} of the asynchronous result.
   */
  Future<Buffer> get();

  /**
   * Closes the configuration store.
   *
   * @return a {@code Future} of the asynchronous result.
   */
  Future<Void> close();
}

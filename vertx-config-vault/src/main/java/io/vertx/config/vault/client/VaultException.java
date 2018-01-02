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

package io.vertx.config.vault.client;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;

/**
 * Exception used when an interaction with Vault failed.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class VaultException extends Exception {

  private final int statusCode;

  public VaultException(String message) {
    super(message);
    statusCode = -1;
  }

  public VaultException(String message, int code) {
    super(message);
    this.statusCode = code;
  }

  public VaultException(String message, Throwable cause) {
    super(message, cause);
    statusCode = -1;
  }

  public static <T> AsyncResult<T> toFailure(String message, Throwable cause) {
    VaultException exception = new VaultException(message, cause);
    return Future.failedFuture(exception);
  }

  public static <T> AsyncResult<T> toFailure(String status, int code, String body) {
    StringBuilder message = new StringBuilder();
    message.append("Vault responded with HTTP status: ").append(status);
    if (body != null && !body.isEmpty()) {
      message.append("\nResponse body:").append(body);
    }
    VaultException exception = new VaultException(message.toString(), code);
    return Future.failedFuture(exception);
  }

  public static <T> AsyncResult<T> toFailure(String message) {
    VaultException exception = new VaultException(message);
    return Future.failedFuture(exception);
  }

  public int getStatusCode() {
    return statusCode;
  }
}

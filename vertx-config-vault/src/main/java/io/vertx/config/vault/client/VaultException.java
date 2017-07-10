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

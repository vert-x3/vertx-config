package io.vertx.config.aws;

/**
 * An exception that is thrown when an AWS Secret is not found
 */
public class AWSMissingSecretException extends Exception {
  public AWSMissingSecretException(String message) {
    super(message);
  }
}

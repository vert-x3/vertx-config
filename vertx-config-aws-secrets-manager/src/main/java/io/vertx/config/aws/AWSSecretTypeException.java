package io.vertx.config.aws;

/**
 * An exception that is thrown when an AWS Secret is not of a usable type
 */
public class AWSSecretTypeException extends Exception {
  public AWSSecretTypeException(String message) { super(message); }
}

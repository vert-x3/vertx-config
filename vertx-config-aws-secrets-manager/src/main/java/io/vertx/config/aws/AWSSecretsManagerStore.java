package io.vertx.config.aws;


import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.*;
import io.vertx.config.spi.ConfigStore;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;

import java.util.Objects;

class AWSSecretsManagerStore implements ConfigStore {
  private final Vertx vertx;
  private final String region;
  private final String secretName;

  public AWSSecretsManagerStore(Vertx vertx, JsonObject configuration) {
    this.vertx = Objects.requireNonNull(vertx);
    this.region = configuration.getString("region");
    this.secretName = configuration.getString("secretName");
  }

  @Override
  public void get(Handler<AsyncResult<Buffer>> completionHandler) {

    try {
      // Check Environment variables exist, but we don't need to use them outselves, AWS SDK will use automatically.
      System.getenv("AWS_ACCESS_KEY_ID");
      System.getenv("AWS_SECRET_ACCESS_KEY");

      vertx.executeBlocking(
        future -> {
          AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard()
            .withRegion(region)
            .build();

          GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
            .withSecretId(secretName);

          try {
            GetSecretValueResult getSecretValueResult = client.getSecretValue(getSecretValueRequest);
            if (getSecretValueResult != null) {
              String secret = getSecretValueResult.getSecretString();

              if (secret != null) {
                future.complete(new JsonObject(secret));
              } else {
                throw new AWSSecretTypeException("Secret is not a string");
              }
            } else {
              throw new AWSMissingSecretException(secretName + " not found");
            }
          } catch (DecodeException | AWSMissingSecretException | DecryptionFailureException | InternalServiceErrorException | InvalidParameterException | InvalidRequestException | ResourceNotFoundException | AWSSecretTypeException e) {
            future.fail(e);
          }
        },
        v -> {
          if (v.failed()) {
            completionHandler.handle(Future.failedFuture(v.cause()));
          } else {
            JsonObject configFromAWSSecretsManager = (JsonObject) v.result();
            completionHandler.handle(Future.succeededFuture(configFromAWSSecretsManager.toBuffer()));
          }
        }
      );

    } catch (NullPointerException e) {
      completionHandler.handle(Future.failedFuture("No AWS environment variable credentials present"));
    }

  }

  @Override
  public void close(Handler<Void> completionHandler) {
    completionHandler.handle(null);
  }
}

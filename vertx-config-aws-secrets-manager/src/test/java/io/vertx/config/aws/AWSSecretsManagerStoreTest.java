package io.vertx.config.aws;


import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * Factory to create {@link AWSSecretsManagerStore} instances.
 *
 * @author <a href="http://indiealexh.com">Alexander Haslam</a>
 */
@RunWith(VertxUnitRunner.class)
public class AWSSecretsManagerStoreTest {

  private ConfigRetriever retriever;
  private Vertx vertx;


  @Before
  public void setUp(TestContext tc) throws IOException {
    vertx = Vertx.vertx();
    vertx.exceptionHandler(tc.exceptionHandler());
  }

  @After
  public void tearDown(TestContext tc) {
    retriever.close();
    vertx.close(tc.asyncAssertSuccess());
  }

  @Test
  public void getExampleFromAWSSecretsManager(TestContext tc) throws Exception {
    Async async = tc.async();
    retriever = ConfigRetriever.create(vertx,
      new ConfigRetrieverOptions().addStore(
        new ConfigStoreOptions()
          .setType("aws-secrets-manager")
          .setConfig(new JsonObject()
            .put("region", "us-west-2")
            .put("secretName", "test/example")
          )));
    retriever.getConfig(ar -> {
      assertThat(ar.succeeded()).isTrue();
      JsonObject json = ar.result();
      Assertions.assertThat(json).isNotNull();
      Assertions.assertThat(json.getString("key")).isEqualTo("value");

      Assertions.assertThat(json.getBoolean("true")).isTrue();
      Assertions.assertThat(json.getBoolean("false")).isFalse();

      Assertions.assertThat(json.getString("missing")).isNull();

      Assertions.assertThat(json.getInteger("int")).isEqualTo(5);
      Assertions.assertThat(json.getDouble("float")).isEqualTo(25.3);

      Assertions.assertThat(json.getJsonArray("array").size()).isEqualTo(3);
      Assertions.assertThat(json.getJsonArray("array").contains(1)).isTrue();
      Assertions.assertThat(json.getJsonArray("array").contains(2)).isTrue();
      Assertions.assertThat(json.getJsonArray("array").contains(3)).isTrue();

      Assertions.assertThat(json.getJsonObject("sub").getString("foo")).isEqualTo("bar");
      async.complete();
    });
  }

}

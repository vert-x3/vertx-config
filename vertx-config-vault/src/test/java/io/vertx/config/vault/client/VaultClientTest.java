package io.vertx.config.vault.client;

import io.vertx.config.vault.utils.VaultProcess;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.UUID;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class VaultClientTest {

  private static VaultProcess process;
  private Vertx vertx;
  private SlimVaultClient client;

  @BeforeClass
  public static void setupClass() throws IOException, InterruptedException {
    process = new VaultProcess();
    process.initAndUnsealVault();
    assert process.isRunning();
  }

  @AfterClass
  public static void tearDownClass() {
    process.shutdown();
  }

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    client = new SlimVaultClient(vertx, process.getConfigurationWithRootToken());
  }

  @After
  public void tearDown(TestContext tc) {
    vertx.close(tc.asyncAssertSuccess());
  }

  /**
   * Write a secret and verify that it can be read.
   */
  @Test
  public void testWriteAndRead(TestContext tc) throws VaultException {
    Async async = tc.async();
    final String path = "secret/hello";
    final String value = "world";

    client.write(path, new JsonObject().put("value", value), x -> {
      tc.assertTrue(x.succeeded());
      client.read(path, ar -> {
        tc.assertTrue(ar.succeeded());
        tc.assertEquals(value, ar.result().getData().getString("value"));
        async.complete();
      });
    });
  }

  /**
   * Write a secret and verify that it can be read containing a null value.
   */
  @Test
  public void testWriteAndReadNull(TestContext tc) throws VaultException {
    final String path = "secret/null";
    final String value = null;

    Async async = tc.async();
    client.write(path, new JsonObject().put("value", value), x -> {
      tc.assertTrue(x.succeeded());
      client.read(path, ar -> {
        tc.assertTrue(ar.succeeded());
        tc.assertNull(ar.result().getData().getString("value"));
        async.complete();
      });
    });
  }

  /**
   * Write a secret, and then verify that its key shows up in the list.
   */
  @Test
  public void testList(TestContext tc) throws VaultException {
    Async async = tc.async();
    client.write("secret/hello", new JsonObject().put("value", "world"), x -> {
      tc.assertTrue(x.succeeded());
      client.list("secret", ar -> {
        tc.assertTrue(ar.succeeded());
        tc.assertTrue(ar.result().contains("hello"));
        async.complete();
      });
    });
  }

  /**
   * Write a secret, and then verify that is is successfully deleted.
   */
  @Test
  public void testDelete(TestContext tc) throws VaultException {
    Async async = tc.async();
    client.write("secret/hello", new JsonObject().put("value", "world"), x -> {
      tc.assertTrue(x.succeeded());
      client.list("secret", y -> {
        tc.assertTrue(y.succeeded());
        tc.assertTrue(y.result().contains("hello"));

        client.delete("secret/hello", z -> {
          tc.assertTrue(z.succeeded());

          client.list("secret", a -> {
            tc.assertTrue(a.succeeded());
            tc.assertFalse(a.result().contains("hello"));
            async.complete();
          });
        });
      });
    });
  }

  /**
   * Tests that exception message includes errors returned by Vault.
   */
  @Test
  public void testReadExceptionMessageIncludesErrorsReturnedByVault(TestContext tc) throws VaultException {
    Async async = tc.async();
    JsonObject configuration = process.getConfiguration();
    configuration.put("token", "this-is-not-the-token");
    client = new SlimVaultClient(vertx, configuration);
    client.read("secret/null", ar -> {
      tc.assertTrue(ar.failed());
      Throwable cause = ar.cause();
      tc.assertNotNull(cause);
      tc.assertTrue(cause instanceof VaultException);
      tc.assertTrue(cause.getMessage().contains("permission denied"));
      async.complete();
    });
  }

  /**
   * Tests that exception message includes errors returned by Vault.
   */
  @Test
  public void testWriteExceptionMessageIncludesErrorsReturnedByVault(TestContext tc) throws VaultException {
    Async async = tc.async();
    JsonObject configuration = process.getConfiguration();
    configuration.put("token", "this-is-not-the-token");
    client = new SlimVaultClient(vertx, configuration);

    client.write("secret/null", new JsonObject().put("value", "foo"), ar -> {
      tc.assertTrue(ar.failed());
      Throwable cause = ar.cause();
      tc.assertTrue(cause instanceof VaultException);
      tc.assertTrue(cause.getMessage().contains("permission denied"));
      async.complete();
    });
  }

  /**
   * Tests that status code are made available upon failures.
   */
  @Test
  public void testReadExceptionMessageIncludesErrorsReturnedByVaultOn404(TestContext tc) throws VaultException {
    Async async = tc.async();

    client.read("secret/" + UUID.randomUUID().toString(), ar -> {
      tc.assertTrue(ar.failed());
      Throwable cause = ar.cause();
      tc.assertNotNull(cause);
      tc.assertTrue(cause instanceof VaultException);
      //noinspection ConstantConditions
      tc.assertEquals(404, ((VaultException) cause).getStatusCode());
      tc.assertTrue(cause.getMessage().contains("\"errors\":[]"));
      async.complete();
    });

  }

  /**
   * Tests that the various supported data types are marshaled/unmarshaled to and from Vault.
   */
  @Test
  public void testWriteAndReadDataTypes(TestContext tc) throws VaultException {
    Async async = tc.async();
    final String path = "secret/hello";

    JsonObject json = new JsonObject();

    json.put("testBoolean", true);
    json.put("testInt", 1001);
    json.put("testFloat", 123.456);
    json.put("testString", "Hello world!");
    json.put("testObject", new JsonObject().put("nestedBool", true).put("nestedInt", 123).put("nestedFloat", 123.456)
      .put("nestedString", "foobar").put("nestedArray", new JsonArray().add("foo").add("bar"))
      .put("nestedObject", new JsonObject().put("foo", "bar"))
    );


    client.write(path, json, x ->
      client.read(path, ar -> {
        tc.assertTrue(ar.succeeded());
        Secret result = ar.result();
        result.getData().fieldNames().forEach(s -> tc.assertTrue(result.getData().getValue(s).equals(json.getValue(s))));
        async.complete();
      }));
  }

  /**
   * Test creation of a new client auth token via a TokenRequest, using the Vault root token.
   * Then, issue a write followed by a read using this token.
   */
  @Test
  public void testCreateTokenWithRequest(TestContext tc) throws VaultException {
    Async async = tc.async();

    client.createToken(new TokenRequest().setTTL("1h"), ar -> {
      System.out.println(ar.result());
      tc.assertTrue(ar.succeeded());
      tc.assertNotNull(ar.result().getToken());
      tc.assertNotNull(ar.result().getAccessor());
      tc.assertEquals(3600L, ar.result().getLeaseDuration());
      tc.assertTrue(ar.result().isRenewable());


      client = new SlimVaultClient(vertx,
        process.getConfiguration().put("token", ar.result().getToken()));

      final String path = "secret/hello";
      final String value = "world " + UUID.randomUUID().toString();

      client.write(path, new JsonObject().put("value", value), x -> {
        tc.assertTrue(x.succeeded());
        client.read(path, ar2 -> {
          tc.assertTrue(ar2.succeeded());
          tc.assertEquals(value, ar2.result().getData().getString("value"));
          async.complete();
        });
      });
    });
  }

  /**
   * Tests token self-renewal for the token auth backend.
   */
  @Test
  public void testRenewSelf(TestContext tc) throws VaultException {
    Async async = tc.async();

    // 1 - Generate a client token
    client.createToken(new TokenRequest().setTTL("1h"), step1 -> {
      tc.assertTrue(step1.succeeded());
      String token = step1.result().getToken();
      tc.assertNotNull(token);
      client = new SlimVaultClient(vertx,
        process.getConfiguration().put("token", token));

      // 2 - renew with -1
      client.renewSelf(-1, step2 -> {
        tc.assertTrue(step2.succeeded());
        String token_2 = step2.result().getToken();
        tc.assertNotNull(token_2);
        tc.assertEquals(token, token_2);
        client = new SlimVaultClient(vertx,
          process.getConfiguration().put("token", token_2));

        // 3 - renew with an explicit increment (duration in second)
        client.renewSelf(20, step3 -> {
          tc.assertTrue(step3.succeeded());
          String token_3 = step3.result().getToken();
          tc.assertNotNull(token_3);
          tc.assertEquals(token, token_2);
          tc.assertEquals(20L, step3.result().getLeaseDuration());

          async.complete();
        });
      });
    });
  }

  /**
   * Tests token lookup-self for the token auth backend.
   */
  @Test
  public void testLookupSelf(TestContext tc) throws VaultException {
    Async async = tc.async();

    // 1 - Generate a client token
    client.createToken(new TokenRequest().setTTL("1h"), step1 -> {
      tc.assertTrue(step1.succeeded());
      String token = step1.result().getToken();
      tc.assertNotNull(token);
      client = new SlimVaultClient(vertx,
        process.getConfiguration().put("token", token));

      // 2 - Lookup
      client.lookupSelf(step2 -> {
        tc.assertTrue(step2.succeeded());
        String token_2 = step2.result().getId();
        tc.assertEquals(token, token_2);
        tc.assertEquals(3600L, step2.result().getCreationTTL());
        tc.assertTrue(step2.result().getTTL() <= 3600);
        async.complete();
      });
    });
  }
}

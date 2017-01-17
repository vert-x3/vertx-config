package io.vertx.ext.configuration.spring;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.configuration.ConfigurationRetriever;
import io.vertx.ext.configuration.ConfigurationRetrieverOptions;
import io.vertx.ext.configuration.ConfigurationStoreOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.config.server.ConfigServerApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * Check the behavior of the Spring config Server store. It starts a Spring Config Server application using the
 * default "sample" location.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class SpringConfigServerStoreTest {

  private ConfigurationRetriever retriever;
  private Vertx vertx;
  private static ConfigurableApplicationContext springConfigServer;


  @BeforeClass
  public static void initSpringConfigServer() {
    springConfigServer = new SpringApplicationBuilder(ConfigServerApplication.class)
        .properties("spring.config.name=configserver").run();
  }


  @AfterClass
  public static void shutdownSpringConfigServer() {
    springConfigServer.close();
  }

  @Before
  public void setUp(TestContext tc) {
    vertx = Vertx.vertx();
    vertx.exceptionHandler(tc.exceptionHandler());
  }


  @After
  public void tearDown() {
    retriever.close();
    vertx.close();
  }

  @Test
  public void testWithFooDev(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigurationRetriever.create(vertx,
        new ConfigurationRetrieverOptions().addStore(
            new ConfigurationStoreOptions()
                .setType("spring-config-server")
                .setConfig(new JsonObject().put("url", "http://localhost:8888/foo/development"))));


    retriever.getConfiguration(json -> {
      System.out.println(json.result().encodePrettily());
      assertThat(json.succeeded()).isTrue();
      JsonObject config = json.result();

      assertThat(config.getString("bar")).isEqualToIgnoringCase("spam");
      assertThat(config.getString("foo")).isEqualToIgnoringCase("from-default");
      assertThat(config.getString("info.description")).isEqualToIgnoringCase("Spring Cloud Samples");

      async.complete();
    });

  }

  @Test
  public void testWithStoresCloud(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigurationRetriever.create(vertx,
        new ConfigurationRetrieverOptions().addStore(
            new ConfigurationStoreOptions()
                .setType("spring-config-server")
                .setConfig(new JsonObject()
                    .put("url", "http://localhost:8888/stores/cloud")
                    .put("timeout", 10000))));


    retriever.getConfiguration(json -> {
      assertThat(json.succeeded()).isTrue();
      JsonObject config = json.result();

      assertThat(config.getInteger("hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds"))
          .isEqualTo(60000);
      assertThat(config.getString("eureka.password")).isEqualToIgnoringCase("password");
      assertThat(config.getString("spring.data.mongodb.uri")).isEqualToIgnoringCase("${vcap.services.${PREFIX:}mongodb.credentials.uri}");
      assertThat(config.getString("eureka.client.serviceUrl.defaultZone"))
          .isEqualToIgnoringCase("http://localhost:8761/eureka/");

      async.complete();
    });

  }

  @Test
  public void testWithUnknownConfiguration(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigurationRetriever.create(vertx,
        new ConfigurationRetrieverOptions().addStore(
            new ConfigurationStoreOptions()
                .setType("spring-config-server")
                .setConfig(new JsonObject()
                    .put("url", "http://localhost:8888/missing/missing")
                    .put("timeout", 10000))));


    retriever.getConfiguration(json -> {
      assertThat(json.succeeded()).isTrue();
      JsonObject config = json.result();
      assertThat(config.getString("eureka.client.serviceUrl.defaultZone"))
          .isEqualToIgnoringCase("http://localhost:8761/eureka/");
      async.complete();
    });
  }

  @Test
  public void testWithErrorConfiguration(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigurationRetriever.create(vertx,
        new ConfigurationRetrieverOptions().addStore(
            new ConfigurationStoreOptions()
                .setType("spring-config-server")
                .setConfig(new JsonObject()
                    .put("url", "http://localhost:8888/missing/missing/missing")
                    .put("timeout", 10000))));


    retriever.getConfiguration(json -> {
      assertThat(json.succeeded()).isFalse();
      async.complete();
    });
  }

  @Test
  public void testWithWrongServerConfiguration(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigurationRetriever.create(vertx,
        new ConfigurationRetrieverOptions().addStore(
            new ConfigurationStoreOptions()
                .setType("spring-config-server")
                .setConfig(new JsonObject()
                    .put("url", "http://not-valid.de")
                    .put("timeout", 10000))));


    retriever.getConfiguration(json -> {
      assertThat(json.succeeded()).isFalse();
      async.complete();
    });
  }

}
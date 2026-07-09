package examples;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(VertxUnitRunner.class)
public class ConfigTomlExamplesTest {

  @Rule
  public RunTestOnContext contextRule = new RunTestOnContext();

  private Vertx vertx;

  @Before
  public void setUp(TestContext context) throws Exception {
    vertx = contextRule.vertx();
  }

  @Test
  public void example1_doesnt_throw(TestContext context) {
    ConfigTomlExamples examples = new ConfigTomlExamples();
    examples.example1(vertx);
  }
}
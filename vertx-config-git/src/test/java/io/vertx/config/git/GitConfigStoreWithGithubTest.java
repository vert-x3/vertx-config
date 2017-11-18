package io.vertx.config.git;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class GitConfigStoreWithGithubTest {

  private static final String REPO = "https://github.com/cescoffier/vertx-config-test.git";

  private Vertx vertx;
  private ConfigRetriever retriever;
  private Git git;

  private File root = new File("target/junk/repo");
  private String branch;
  private String remote = "origin";

  @Before
  public void setUp(TestContext context) throws IOException, GitAPIException {
    vertx = Vertx.vertx();
    vertx.exceptionHandler(context.exceptionHandler());

    FileUtils.deleteDirectory(new File("target/junk"));

    git = connect(root);
  }

  @After
  public void tearDown() {
    AtomicBoolean done = new AtomicBoolean();
    if (retriever != null) {
      retriever.close();
    }

    if (git != null) {
      git.close();
    }

    vertx.close(v -> done.set(true));

    await().untilAtomic(done, is(true));
  }

  @Test
  public void testOnMasterWithASingleFile(TestContext tc) throws GitAPIException, IOException {
    Async async = tc.async();

    retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions().addStore(new
        ConfigStoreOptions().setType("git").setConfig(new JsonObject()
        .put("url", REPO)
        .put("path", "target/junk/work")
        .put("filesets", new JsonArray().add(new JsonObject().put("pattern", "a.json"))))));

    retriever.getConfig(ar -> {
      assertThat(ar.succeeded()).isTrue();
      assertThat(ar.result()).isNotEmpty();
      JsonObject json = ar.result();
      assertThat(json).isNotNull();
      assertThat(json.getString("branch")).isEqualToIgnoringCase("master");
      assertThat(json.getString("name")).isEqualToIgnoringCase("A");
      async.complete();
    });

  }

  @Test
  public void testOnDevWithATwoFiles(TestContext tc) throws GitAPIException, IOException {
    Async async = tc.async();

    retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions().addStore(new
        ConfigStoreOptions().setType("git").setConfig(new JsonObject()
        .put("url", REPO)
        .put("path", "target/junk/work")
        .put("branch", "dev")
        .put("filesets", new JsonArray().add(new JsonObject().put("pattern", "*.json"))))));

    retriever.getConfig(ar -> {
      assertThat(ar.succeeded()).isTrue();
      assertThat(ar.result()).isNotEmpty();
      JsonObject json = ar.result();
      assertThat(json).isNotNull();
      assertThat(json.getString("branch")).isEqualToIgnoringCase("dev");
      assertThat(json.getString("key")).isEqualToIgnoringCase("value");
      assertThat(json.getString("keyB")).isEqualToIgnoringCase("valueB");
      assertThat(json.getString("name")).isEqualToIgnoringCase("B");
      async.complete();
    });
  }

  private Git connect(File root) throws MalformedURLException, GitAPIException {
    return Git.cloneRepository()
        .setURI(REPO)
        .setRemote(remote)
        .setDirectory(root).call();
  }
}

package io.vertx.ext.configuration.git;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.configuration.spi.ConfigurationStore;
import io.vertx.ext.configuration.utils.FileSet;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class GitConfigurationStore implements ConfigurationStore {

  private final static Logger LOGGER
      = LoggerFactory.getLogger(GitConfigurationStore.class);

  private final Vertx vertx;
  private final File path;
  private final List<FileSet> filesets = new ArrayList<>();
  private final String url;
  private final String branch;
  private final String remote;
  private final Git git;

  public GitConfigurationStore(Vertx vertx, JsonObject configuration) {
    this.vertx = vertx;

    String path = Objects.requireNonNull(configuration.getString("path"),
        "The `path` configuration is required.");
    this.path = new File(path);
    if (this.path.isFile()) {
      throw new IllegalArgumentException("The `path` must not be a file");
    }

    JsonArray filesets = Objects.requireNonNull(configuration
            .getJsonArray("filesets"),
        "The `filesets` element is required.");

    for (Object o : filesets) {
      JsonObject json = (JsonObject) o;
      FileSet set = new FileSet(vertx, this.path, json);
      this.filesets.add(set);
    }

    // Git repository
    url = Objects.requireNonNull(configuration.getString("url"),
        "The `url` configuration (Git repository location) is required.");
    branch = configuration.getString("branch", "master");
    remote = configuration.getString("remote", "origin");

    try {
      git = initializeGit();
    } catch (Exception e) {
      throw new IllegalStateException("Unable to initialize the Git" +
          " repository", e);
    }
  }

  private Git initializeGit() throws IOException, GitAPIException {
    if (path.isDirectory()) {
      Git git = Git.open(path);
      String current = git.getRepository().getBranch();
      if (branch.equalsIgnoreCase(current)) {
        PullResult pull = git.pull().setRemote(remote).call();
        if (!pull.isSuccessful()) {
          LOGGER.warn("Unable to pull the branch + '" + branch +
              "' from the remote repository '" + remote + "'");
        }
        return git;
      } else {
        git.checkout().
            setName(branch).
            setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK).
            setStartPoint(remote + "/" + branch).
            call();
        return git;
      }
    } else {
      return Git.cloneRepository()
          .setURI(url)
          .setBranch(branch)
          .setRemote(remote)
          .setDirectory(path)
          .call();
    }
  }


  @Override
  public void get(Handler<AsyncResult<Buffer>> completionHandler) {
    update()   // Update repository
        .compose(v -> read()) // Read files
        .compose(this::compute)  // Compute the merged json
        .setHandler(ar -> {     // Forward
          if (ar.failed()) {
            completionHandler.handle(Future.failedFuture(ar.cause()));
          } else {
            completionHandler.handle(Future.succeededFuture(ar.result()));
          }
        });
  }

  private Future<Buffer> compute(List<File> files) {
    Future<Buffer> result = Future.future();

    List<Future> futures = new ArrayList<>();
    for (FileSet set : filesets) {
      Future<JsonObject> future = Future.future();
      set.buildConfiguration(files, json -> {
        if (json.failed()) {
          future.fail(json.cause());
        } else {
          future.complete(json.result());
        }
      });
      futures.add(future);
    }

    CompositeFuture.all(futures).setHandler(cf -> {
      if (cf.failed()) {
        result.fail(cf.cause());
      } else {
        JsonObject json = new JsonObject();
        futures.stream().map(f -> (JsonObject) f.result())
            .forEach(json::mergeIn);
        result.complete(Buffer.buffer(json.encode()));
      }
    });

    return result;
  }

  private Future<Void> update() {
    Future<Void> result = Future.future();
    vertx.executeBlocking(
        future -> {
          PullResult call = null;
          try {
            call = git.pull().setRemote(remote).setRemoteBranchName(branch).call();
          } catch (GitAPIException e) {
            future.fail(e);
            return;
          }
          if (call.isSuccessful()) {
            future.complete();
          } else {
            future.fail("Unable to merge repository - Conflicts: "
                + call.getMergeResult().getCheckoutConflicts());
          }
        },
        result.completer()
    );
    return result;
  }

  private Future<List<File>> read() {
    Future<List<File>> result = Future.future();
    vertx.executeBlocking(
        fut -> {
          try {
            fut.complete(FileSet.traverse(path));
          } catch (Throwable e) {
            fut.fail(e);
          }
        },
        result.completer());
    return result;
  }

  @Override
  public void close(Handler<Void> completionHandler) {
    vertx.runOnContext(v -> {
      if (git != null) {
        git.close();
      }
      completionHandler.handle(null);
    });
  }
}

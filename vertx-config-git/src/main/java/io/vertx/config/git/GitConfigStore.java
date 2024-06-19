/*
 * Copyright (c) 2014 Red Hat, Inc. and others
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package io.vertx.config.git;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import io.vertx.config.spi.ConfigStore;
import io.vertx.config.spi.utils.FileSet;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.internal.logging.Logger;
import io.vertx.core.internal.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.util.FS;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class GitConfigStore implements ConfigStore {

  private final static Logger LOGGER
    = LoggerFactory.getLogger(GitConfigStore.class);

  private final VertxInternal vertx;
  private final File path;
  private final List<FileSet> filesets = new ArrayList<>();
  private final String url;
  private final String branch;
  private final String remote;
  private final Git git;
  private final CredentialsProvider credentialProvider;
  private final TransportConfigCallback transportConfigCallback;

  public GitConfigStore(Vertx vertx, JsonObject configuration) {
    this.vertx = (VertxInternal) vertx;

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

    if (Objects.nonNull(configuration.getString("user")) &&
	       Objects.nonNull(configuration.getString("password"))) {
      credentialProvider = new UsernamePasswordCredentialsProvider(
        configuration.getString("user"), configuration.getString("password"));
    } else {
      credentialProvider = null;
    }
    if(Objects.nonNull(configuration.getString("idRsaKeyPath"))){
      SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
        @Override
        protected void configure(OpenSshConfig.Host host, Session session ) {
        }
        @Override
        protected JSch createDefaultJSch(FS fs ) throws JSchException {
          JSch defaultJSch = super.createDefaultJSch( fs );
          defaultJSch.setConfig("StrictHostKeyChecking", "no");
          defaultJSch.addIdentity(configuration.getString("idRsaKeyPath"));
          return defaultJSch;
        }
      };
      transportConfigCallback = new TransportConfigCallback() {
        @Override
        public void configure( Transport transport ) {
          SshTransport sshTransport = ( SshTransport )transport;
          sshTransport.setSshSessionFactory( sshSessionFactory );
        }
      };
    }else {
      transportConfigCallback = null;
    }

    try {
      git = initializeGit();
    } catch (Exception e) {
      throw new VertxException("Unable to initialize the Git repository", e);
    }
  }

  private Git initializeGit() throws IOException, GitAPIException {
    if (path.isDirectory()) {
      Git git = Git.open(path);
      String current = git.getRepository().getBranch();
      if (branch.equalsIgnoreCase(current)) {
        PullResult pull = git.pull().setRemote(remote).setCredentialsProvider(credentialProvider)
          .setTransportConfigCallback(transportConfigCallback).call();
        if (!pull.isSuccessful()) {
          LOGGER.warn("Unable to pull the branch + '" + branch +
            "' from the remote repository '" + remote + "'");
        }
        return git;
      } else {
        git.checkout()
          .setName(branch)
          .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
          .setStartPoint(remote + "/" + branch)
          .call();
        return git;
      }
    } else {
      return Git.cloneRepository()
        .setURI(url)
        .setBranch(branch)
        .setRemote(remote)
        .setDirectory(path)
        .setCredentialsProvider(credentialProvider)
        .setTransportConfigCallback(transportConfigCallback)
        .call();
    }
  }


  @Override
  public Future<Buffer> get() {
    return update()   // Update repository
      .compose(v -> read()) // Read files
      .compose(this::compute);  // Compute the merged json
  }

  private Future<Buffer> compute(List<File> files) {
    List<Future<JsonObject>> futures = new ArrayList<>();
    for (FileSet set : filesets) {
      Promise<JsonObject> future = Promise.promise();
      set.buildConfiguration(files, json -> {
        if (json.failed()) {
          future.fail(json.cause());
        } else {
          future.complete(json.result());
        }
      });
      futures.add(future.future());
    }

    return Future.all(futures).map(compositeFuture -> {
      JsonObject json = new JsonObject();
      compositeFuture.<JsonObject>list().stream().forEach(config -> json.mergeIn(config, true));
      return json.toBuffer();
    });
  }

  private Future<Void> update() {
    return vertx.executeBlocking(() -> git.pull().setRemote(remote).setRemoteBranchName(branch).setCredentialsProvider(credentialProvider)
      .setTransportConfigCallback(transportConfigCallback).call()).flatMap(call -> {
      if (call.isSuccessful()) {
        return Future.succeededFuture();
      }
      if (call.getMergeResult() != null) {
        return Future.failedFuture("Unable to merge repository - Conflicts: "
          + call.getMergeResult().getCheckoutConflicts());
      }
      return Future.failedFuture("Unable to rebase repository - Conflicts: "
        + call.getRebaseResult().getConflicts());
    });
  }

  private Future<List<File>> read() {
    return vertx.executeBlocking(() -> FileSet.traverse(path).stream().sorted().collect(toList()));
  }

  @Override
  public Future<Void> close() {
    return vertx.executeBlocking(() -> {
      git.close();
      return null;
    });
  }
}

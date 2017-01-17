package io.vertx.ext.configuration.git;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.configuration.spi.ConfigurationStore;
import io.vertx.ext.configuration.spi.ConfigurationStoreFactory;
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
public class GitConfigurationStoreFactory implements ConfigurationStoreFactory {


  @Override
  public String name() {
    return "git";
  }

  @Override
  public ConfigurationStore create(Vertx vertx, JsonObject configuration) {
    return new GitConfigurationStore(vertx, configuration);
  }
}

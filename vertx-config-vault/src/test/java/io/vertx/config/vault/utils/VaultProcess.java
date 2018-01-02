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

package io.vertx.config.vault.utils;

import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.PemKeyCertOptions;
import org.apache.commons.exec.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.core.Is.is;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class VaultProcess {


  public static final String VAULT_VERSION = "0.7.3";
  public static final String CA_CERT_ARG = "-ca-cert=target/vault/config/ssl/cert.pem";
  private File executable;
  private String unseal;
  private String token;
  private ExecuteWatchdog watchDog;
  private String backend;

  public VaultProcess() {
    executable = VaultDownloader.download();
    try {
      Certificates.createVaultCertAndKey();
      Certificates.createClientCertAndKey();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void initAndUnsealVault() {
    backend = null;
    File stored = new File("target/vault/file");
    FileUtils.deleteQuietly(stored);
    startServer();
    init();
    unseal();
  }

  private void init() {
    String line = executable.getAbsolutePath() + " init -key-shares=1 -key-threshold=1 " + CA_CERT_ARG;
    System.out.println(">> " + line);
    CommandLine parse = CommandLine.parse(line);
    DefaultExecutor executor = new DefaultExecutor();
    PumpStreamHandler pump = new PumpStreamHandler(new VaultOutputStream().addExtractor(
      l -> {
        if (l.contains("Unseal Key 1:")) {
          unseal = l.replace("Unseal Key 1: ", "").trim();
        } else if (l.contains("Initial Root Token:")) {
          token = l.replace("Initial Root Token: ", "").trim();
        }
      }
    ), System.err);

    ExecuteWatchdog watchDog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
    executor.setWatchdog(watchDog);
    executor.setStreamHandler(pump);
    try {
      executor.execute(parse);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

//    await().until(() -> token != null);
//    await().until(() -> unseal != null);

    System.out.println("Vault Server initialized (but sealed)");
    System.out.println("Root token: " + token);
    System.out.println("Unseal key: " + unseal);
  }

  public void unseal() {
    run("unseal " + CA_CERT_ARG + " " + unseal);
    System.out.println("Vault Server ready !");
  }

  private void startServer() {
    String line = executable.getAbsolutePath() + " server -config=src/test/resources/config.json";
    System.out.println(">> " + line);
    CommandLine parse = CommandLine.parse(line);
    DefaultExecutor executor = new DefaultExecutor();
    DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
    AtomicBoolean ready = new AtomicBoolean();
    PumpStreamHandler pump = new PumpStreamHandler(new VaultOutputStream().addExtractor(
      l -> {
        if (l.contains("Vault server started!")) {
          ready.set(true);
        }
      }
    ), System.err);

    watchDog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
    executor.setWatchdog(watchDog);
    executor.setStreamHandler(pump);
    try {
      executor.execute(parse, resultHandler);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    await().untilAtomic(ready, is(true));
    System.out.println("Vault Server ready - but not yet initialized");
  }


  public void shutdown() {
    if (watchDog != null) {
      watchDog.destroyProcess();
    }
  }

  public boolean run(String args) {
    String cli = executable.getAbsolutePath() + " " + args;
    System.out.println(">> " + cli);
    CommandLine parse = CommandLine.parse(cli);
    DefaultExecutor executor = new DefaultExecutor();
    executor.setExitValue(0);
    try {
      return executor.execute(parse) == 0;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean isRunning() {
    return run("status " + CA_CERT_ARG);
  }

  public String getHost() {
    return "127.0.0.1";
  }

  public int getPort() {
    return 8200;
  }

  public JsonObject getConfiguration() {
    JsonObject config = new JsonObject();
    config.put("host", getHost());
    config.put("port", getPort());
    config.put("ssl", true);
    PemKeyCertOptions options = new PemKeyCertOptions()
      .addCertPath("target/vault/config/ssl/client-cert.pem")
      .addKeyPath("target/vault/config/ssl/client-privatekey.pem");
    config.put("pemKeyCertOptions", options.toJson());
    JksOptions jks = new JksOptions()
      .setPath("target/vault/config/ssl/truststore.jks");
    config.put("trustStoreOptions", jks.toJson());
    return config;
  }

  public JsonObject getConfigurationWithRootToken() {
    return getConfiguration().put("token", token);
  }

  public void setupBackendAppRole() {
    if ("appRole".equals(backend)) {
      return;
    }
    run("auth " + CA_CERT_ARG + " " + token);
    // Add a "user" policy for testing purpose.
    run("policy-write " + CA_CERT_ARG + " user src/test/resources/acl.hcl");
    run("auth-enable " + CA_CERT_ARG + " approle");
    run("write " + CA_CERT_ARG + " auth/approle/role/testrole secret_id_ttl=10m token_ttlc=20m " +
      "token_max_ttl=30m secret_id_num_users=40 policies=user");
    backend = "appRole";
  }

  public void setupBackendCert() {
    if ("cert".equalsIgnoreCase(backend)) {
      return;
    }

    run("auth " + CA_CERT_ARG + " " + token);
    // Add a "user" policy for testing purpose.
    run("policy-write " + CA_CERT_ARG + " user src/test/resources/acl.hcl");

    run("auth-enable " + CA_CERT_ARG + " cert");
    run("write " + CA_CERT_ARG + " auth/cert/certs/web display_name=web " +
      "policies=web,prod,user certificate=@target/vault/config/ssl/client-cert.pem ttl=3600");

    backend = "cert";
  }

  public void setupBackendUserPass() {
    if ("userpass".equalsIgnoreCase(backend)) {
      return;
    }

    run("auth " + CA_CERT_ARG + " " + token);
    // Add a "user" policy for testing purpose.
    run("policy-write " + CA_CERT_ARG + " user src/test/resources/acl.hcl");

    run("auth-enable " + CA_CERT_ARG + " userpass");
    run("write " + CA_CERT_ARG + " auth/userpass/users/fake-user password=fake-password policies=user");

    backend = "userpass";
  }

  public String getToken() {
    return token;
  }

  public String getUnsealKey() {
    return unseal;
  }

  public void runAndProcess(String command, Consumer<String> processor) {
    String cli = executable.getAbsolutePath() + " " + command;
    System.out.println(">> " + cli);
    CommandLine parse = CommandLine.parse(cli);
    DefaultExecutor executor = new DefaultExecutor();
    PumpStreamHandler pump = new PumpStreamHandler(new VaultOutputStream().addExtractor(processor), System.err);

    ExecuteWatchdog watchDog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
    executor.setWatchdog(watchDog);
    executor.setStreamHandler(pump);
    try {
      executor.execute(parse);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


  private class VaultOutputStream extends OutputStream {

    StringBuilder last = new StringBuilder();
    List<Consumer<String>> extractors = new ArrayList<>();

    public VaultOutputStream addExtractor(Consumer<String> extractor) {
      extractors.add(extractor);
      return this;
    }

    @Override
    public synchronized void write(int b) throws IOException {
      last.append((char) b);

      if ((char) b == '\n') {
        String line = last.toString();
        System.out.println(line);
        for (Consumer f : extractors) {
          f.accept(line);
        }
        last = new StringBuilder();
      }
    }


  }


}

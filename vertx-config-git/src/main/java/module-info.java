/*
 * Copyright (c) 2024 Red Hat, Inc. and others
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
import io.vertx.config.spi.ConfigStoreFactory;

module io.vertx.config.git {

  requires static io.vertx.docgen;
  requires static io.vertx.codegen.api;
  requires static io.vertx.codegen.json;

  requires io.vertx.config;
  requires io.vertx.core;
  requires io.vertx.core.logging;
  requires jsch; // automatic
  requires org.eclipse.jgit; // named automatic
  requires org.eclipse.jgit.ssh.jsch; // named automatic

  exports io.vertx.config.git;

  provides ConfigStoreFactory with io.vertx.config.git.GitConfigStoreFactory;
}

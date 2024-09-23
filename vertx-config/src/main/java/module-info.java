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
module io.vertx.config {

  requires static io.vertx.docgen;
  requires static io.vertx.codegen.api;
  requires static io.vertx.codegen.json;

  requires io.vertx.core;
  requires io.vertx.core.logging;

  exports io.vertx.config;
  exports io.vertx.config.spi;

  exports io.vertx.config.spi.utils to
    io.vertx.config.hocon,
    io.vertx.config.configmap,
    io.vertx.config.git,
    io.vertx.config.consul;

  exports io.vertx.config.impl to io.vertx.config.tests;
  exports io.vertx.config.impl.spi to io.vertx.config.tests;

  uses io.vertx.config.spi.ConfigProcessor;
  uses io.vertx.config.spi.ConfigStoreFactory;

  provides io.vertx.config.spi.ConfigProcessor with
    io.vertx.config.impl.spi.JsonProcessor,
    io.vertx.config.impl.spi.RawProcessor,
    io.vertx.config.impl.spi.PropertiesConfigProcessor;

  provides io.vertx.config.spi.ConfigStoreFactory with
    io.vertx.config.impl.spi.FileConfigStoreFactory,
    io.vertx.config.impl.spi.JsonConfigStoreFactory,
    io.vertx.config.impl.spi.EnvVariablesConfigStoreFactory,
    io.vertx.config.impl.spi.SystemPropertiesConfigStoreFactory,
    io.vertx.config.impl.spi.HttpConfigStoreFactory,
    io.vertx.config.impl.spi.EventBusConfigStoreFactory,
    io.vertx.config.impl.spi.DirectoryConfigStoreFactory;

}

/*
 * Copyright 2014 Red Hat, Inc.
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
 */

/** @module vertx-configuration-js/configuration_service */
var utils = require('vertx-js/util/utils');
var Vertx = require('vertx-js/vertx');
var ConfigurationStream = require('vertx-configuration-js/configuration_stream');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JConfigurationService = io.vertx.ext.configuration.ConfigurationService;
var ConfigurationServiceOptions = io.vertx.ext.configuration.ConfigurationServiceOptions;
var ConfigurationChange = io.vertx.ext.configuration.ConfigurationChange;

/**

 @class
*/
var ConfigurationService = function(j_val) {

  var j_configurationService = j_val;
  var that = this;

  /**
   Reads the configuration from the different {@link ConfigurationStore}
   and computes the final configuration.

   @public
   @param completionHandler {function} handler receiving the computed configuration, or a failure if the configuration cannot be retrieved 
   */
  this.getConfiguration = function(completionHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_configurationService["getConfiguration(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        completionHandler(utils.convReturnJson(ar.result()), null);
      } else {
        completionHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Closes the service.

   @public

   */
  this.close = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_configurationService["close()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Gets the last computed configuration.

   @public

   @return {Object} the last configuration
   */
  this.getCachedConfiguration = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnJson(j_configurationService["getCachedConfiguration()"]());
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Registers a listener receiving configuration changes. This method cannot only be called if
   the configuration is broadcasted.

   @public
   @param listener {function} the listener 
   */
  this.listen = function(listener) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_configurationService["listen(io.vertx.core.Handler)"](function(jVal) {
      listener(utils.convReturnDataObject(jVal));
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   @return the stream of configurations.

   @public

   @return {ConfigurationStream}
   */
  this.configurationStream = function() {
    var __args = arguments;
    if (__args.length === 0) {
      if (that.cachedconfigurationStream == null) {
        that.cachedconfigurationStream = utils.convReturnVertxGen(j_configurationService["configurationStream()"](), ConfigurationStream);
      }
      return that.cachedconfigurationStream;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_configurationService;
};

/**
 Creates an instance of the default implementation of the {@link ConfigurationService}.

 @memberof module:vertx-configuration-js/configuration_service
 @param vertx {Vertx} the vert.x instance 
 @param options {Object} the options, must not be <code>null</code>, must contain the list of configured store. 
 @return {ConfigurationService} the created instance.
 */
ConfigurationService.create = function() {
  var __args = arguments;
  if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
    return utils.convReturnVertxGen(JConfigurationService["create(io.vertx.core.Vertx)"](__args[0]._jdel), ConfigurationService);
  }else if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && (typeof __args[1] === 'object' && __args[1] != null)) {
    return utils.convReturnVertxGen(JConfigurationService["create(io.vertx.core.Vertx,io.vertx.ext.configuration.ConfigurationServiceOptions)"](__args[0]._jdel, __args[1] != null ? new ConfigurationServiceOptions(new JsonObject(JSON.stringify(__args[1]))) : null), ConfigurationService);
  } else throw new TypeError('function invoked with invalid arguments');
};

// We export the Constructor function
module.exports = ConfigurationService;
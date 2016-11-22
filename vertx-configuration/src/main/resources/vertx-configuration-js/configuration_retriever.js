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

/** @module vertx-configuration-js/configuration_retriever */
var utils = require('vertx-js/util/utils');
var Vertx = require('vertx-js/vertx');
var Future = require('vertx-js/future');
var ConfigurationStream = require('vertx-configuration-js/configuration_stream');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JConfigurationRetriever = io.vertx.ext.configuration.ConfigurationRetriever;
var ConfigurationRetrieverOptions = io.vertx.ext.configuration.ConfigurationRetrieverOptions;
var ConfigurationChange = io.vertx.ext.configuration.ConfigurationChange;

/**
 Defines a configuration retriever that read configuration from
 @class
*/
var ConfigurationRetriever = function(j_val) {

  var j_configurationRetriever = j_val;
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
      j_configurationRetriever["getConfiguration(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        completionHandler(utils.convReturnJson(ar.result()), null);
      } else {
        completionHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Same as {@link ConfigurationRetriever#getConfiguration}, but returning a  object. The result is a
   .

   @public

   @return {Future}
   */
  this.getConfigurationFuture = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnVertxGen(Future, j_configurationRetriever["getConfigurationFuture()"](), undefined);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Closes the retriever.

   @public

   */
  this.close = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_configurationRetriever["close()"]();
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
      return utils.convReturnJson(j_configurationRetriever["getCachedConfiguration()"]());
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
      j_configurationRetriever["listen(io.vertx.core.Handler)"](function(jVal) {
      listener(utils.convReturnDataObject(jVal));
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {ConfigurationStream} the stream of configurations.
   */
  this.configurationStream = function() {
    var __args = arguments;
    if (__args.length === 0) {
      if (that.cachedconfigurationStream == null) {
        that.cachedconfigurationStream = utils.convReturnVertxGen(ConfigurationStream, j_configurationRetriever["configurationStream()"]());
      }
      return that.cachedconfigurationStream;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_configurationRetriever;
};

ConfigurationRetriever._jclass = utils.getJavaClass("io.vertx.ext.configuration.ConfigurationRetriever");
ConfigurationRetriever._jtype = {
  accept: function(obj) {
    return ConfigurationRetriever._jclass.isInstance(obj._jdel);
  },
  wrap: function(jdel) {
    var obj = Object.create(ConfigurationRetriever.prototype, {});
    ConfigurationRetriever.apply(obj, arguments);
    return obj;
  },
  unwrap: function(obj) {
    return obj._jdel;
  }
};
ConfigurationRetriever._create = function(jdel) {
  var obj = Object.create(ConfigurationRetriever.prototype, {});
  ConfigurationRetriever.apply(obj, arguments);
  return obj;
}
/**
 Creates an instance of the default implementation of the {@link ConfigurationRetriever}.

 @memberof module:vertx-configuration-js/configuration_retriever
 @param vertx {Vertx} the vert.x instance 
 @param options {Object} the options, must not be <code>null</code>, must contain the list of configured store. 
 @return {ConfigurationRetriever} the created instance.
 */
ConfigurationRetriever.create = function() {
  var __args = arguments;
  if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
    return utils.convReturnVertxGen(ConfigurationRetriever, JConfigurationRetriever["create(io.vertx.core.Vertx)"](__args[0]._jdel));
  }else if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && (typeof __args[1] === 'object' && __args[1] != null)) {
    return utils.convReturnVertxGen(ConfigurationRetriever, JConfigurationRetriever["create(io.vertx.core.Vertx,io.vertx.ext.configuration.ConfigurationRetrieverOptions)"](__args[0]._jdel, __args[1] != null ? new ConfigurationRetrieverOptions(new JsonObject(JSON.stringify(__args[1]))) : null));
  } else throw new TypeError('function invoked with invalid arguments');
};

module.exports = ConfigurationRetriever;
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

/** @module vertx-config-js/config_retriever */
var utils = require('vertx-js/util/utils');
var Vertx = require('vertx-js/vertx');
var ReadStream = require('vertx-js/read_stream');
var Future = require('vertx-js/future');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JConfigRetriever = Java.type('io.vertx.config.ConfigRetriever');
var ConfigRetrieverOptions = Java.type('io.vertx.config.ConfigRetrieverOptions');
var ConfigChange = Java.type('io.vertx.config.ConfigChange');

/**
 Defines a configuration retriever that read configuration from
 @class
*/
var ConfigRetriever = function(j_val) {

  var j_configRetriever = j_val;
  var that = this;

  /**
   Reads the configuration from the different 
   and computes the final configuration.

   @public
   @param completionHandler {function} handler receiving the computed configuration, or a failure if the configuration cannot be retrieved 
   */
  this.getConfig = function(completionHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_configRetriever["getConfig(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        completionHandler(utils.convReturnJson(ar.result()), null);
      } else {
        completionHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Closes the retriever.

   @public

   */
  this.close = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_configRetriever["close()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Gets the last computed configuration.

   @public

   @return {Object} the last configuration
   */
  this.getCachedConfig = function() {
    var __args = arguments;
    if (__args.length === 0) {
      return utils.convReturnJson(j_configRetriever["getCachedConfig()"]());
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
      j_configRetriever["listen(io.vertx.core.Handler)"](function(jVal) {
      listener(utils.convReturnDataObject(jVal));
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {ReadStream} the stream of configurations. It's single stream (unicast) and that delivers the last known config and the successors periodically.
   */
  this.configStream = function() {
    var __args = arguments;
    if (__args.length === 0) {
      if (that.cachedconfigStream == null) {
        that.cachedconfigStream = utils.convReturnVertxGen(ReadStream, j_configRetriever["configStream()"](), undefined);
      }
      return that.cachedconfigStream;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_configRetriever;
};

ConfigRetriever._jclass = utils.getJavaClass("io.vertx.config.ConfigRetriever");
ConfigRetriever._jtype = {
  accept: function(obj) {
    return ConfigRetriever._jclass.isInstance(obj._jdel);
  },
  wrap: function(jdel) {
    var obj = Object.create(ConfigRetriever.prototype, {});
    ConfigRetriever.apply(obj, arguments);
    return obj;
  },
  unwrap: function(obj) {
    return obj._jdel;
  }
};
ConfigRetriever._create = function(jdel) {
  var obj = Object.create(ConfigRetriever.prototype, {});
  ConfigRetriever.apply(obj, arguments);
  return obj;
}
/**
 Creates an instance of the default implementation of the {@link ConfigRetriever}.

 @memberof module:vertx-config-js/config_retriever
 @param vertx {Vertx} the vert.x instance 
 @param options {Object} the options, must not be <code>null</code>, must contain the list of configured store. 
 @return {ConfigRetriever} the created instance.
 */
ConfigRetriever.create = function() {
  var __args = arguments;
  if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
    return utils.convReturnVertxGen(ConfigRetriever, JConfigRetriever["create(io.vertx.core.Vertx)"](__args[0]._jdel));
  }else if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && (typeof __args[1] === 'object' && __args[1] != null)) {
    return utils.convReturnVertxGen(ConfigRetriever, JConfigRetriever["create(io.vertx.core.Vertx,io.vertx.config.ConfigRetrieverOptions)"](__args[0]._jdel, __args[1] != null ? new ConfigRetrieverOptions(new JsonObject(Java.asJSONCompatible(__args[1]))) : null));
  } else throw new TypeError('function invoked with invalid arguments');
};

/**
 Same as {@link ConfigRetriever#getConfig}, but returning a  object. The result is a
 .

 @memberof module:vertx-config-js/config_retriever
 @param retriever {ConfigRetriever} the config retrieve 
 @return {Future} the future completed when the configuration is retrieved
 */
ConfigRetriever.getConfigAsFuture = function(retriever) {
  var __args = arguments;
  if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
    return utils.convReturnVertxGen(Future, JConfigRetriever["getConfigAsFuture(io.vertx.config.ConfigRetriever)"](retriever._jdel), undefined);
  } else throw new TypeError('function invoked with invalid arguments');
};

module.exports = ConfigRetriever;
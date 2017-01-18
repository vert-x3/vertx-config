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

/** @module vertx-config-js/config_stream */
var utils = require('vertx-js/util/utils');
var ReadStream = require('vertx-js/read_stream');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JConfigStream = io.vertx.config.ConfigStream;

/**

 @class
*/
var ConfigStream = function(j_val) {

  var j_configStream = j_val;
  var that = this;
  ReadStream.call(this, j_val);

  /**

   @public

   @return {ReadStream}
   */
  this.pause = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_configStream["pause()"]();
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public

   @return {ReadStream}
   */
  this.resume = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_configStream["resume()"]();
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param handler {function} 
   @return {ConfigStream}
   */
  this.exceptionHandler = function(handler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_configStream["exceptionHandler(io.vertx.core.Handler)"](function(jVal) {
      handler(utils.convReturnThrowable(jVal));
    });
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param handler {function} 
   @return {ConfigStream}
   */
  this.handler = function(handler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_configStream["handler(io.vertx.core.Handler)"](function(jVal) {
      handler(utils.convReturnJson(jVal));
    });
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param endHandler {function} 
   @return {ConfigStream}
   */
  this.endHandler = function(endHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_configStream["endHandler(io.vertx.core.Handler)"](endHandler);
      return that;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_configStream;
};

ConfigStream._jclass = utils.getJavaClass("io.vertx.config.ConfigStream");
ConfigStream._jtype = {
  accept: function(obj) {
    return ConfigStream._jclass.isInstance(obj._jdel);
  },
  wrap: function(jdel) {
    var obj = Object.create(ConfigStream.prototype, {});
    ConfigStream.apply(obj, arguments);
    return obj;
  },
  unwrap: function(obj) {
    return obj._jdel;
  }
};
ConfigStream._create = function(jdel) {
  var obj = Object.create(ConfigStream.prototype, {});
  ConfigStream.apply(obj, arguments);
  return obj;
}
module.exports = ConfigStream;
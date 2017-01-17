package io.vertx.kotlin.ext.configuration

import io.vertx.ext.configuration.ConfigurationStoreOptions

fun ConfigurationStoreOptions(
    config: io.vertx.core.json.JsonObject? = null,
  format: String? = null,
  type: String? = null): ConfigurationStoreOptions = io.vertx.ext.configuration.ConfigurationStoreOptions().apply {

  if (config != null) {
    this.config = config
  }

  if (format != null) {
    this.format = format
  }

  if (type != null) {
    this.type = type
  }

}


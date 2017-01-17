package io.vertx.kotlin.ext.configuration

import io.vertx.config.ConfigurationStoreOptions

fun ConfigurationStoreOptions(
    config: io.vertx.core.json.JsonObject? = null,
  format: String? = null,
  type: String? = null): ConfigurationStoreOptions = ConfigurationStoreOptions().apply {

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


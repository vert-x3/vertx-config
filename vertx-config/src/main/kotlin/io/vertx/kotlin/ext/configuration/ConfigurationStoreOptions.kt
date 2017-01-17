package io.vertx.kotlin.ext.configuration

import io.vertx.config.ConfigStoreOptions

fun ConfigurationStoreOptions(
    config: io.vertx.core.json.JsonObject? = null,
  format: String? = null,
  type: String? = null): ConfigStoreOptions = ConfigStoreOptions().apply {

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


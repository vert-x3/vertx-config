package io.vertx.kotlin.config

import io.vertx.config.ConfigStoreOptions

fun ConfigStoreOptions(
    config: io.vertx.core.json.JsonObject? = null,
  format: String? = null,
  type: String? = null): ConfigStoreOptions = io.vertx.config.ConfigStoreOptions().apply {

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


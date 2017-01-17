package io.vertx.kotlin.config

import io.vertx.config.ConfigurationChange

fun ConfigurationChange(
    newConfiguration: io.vertx.core.json.JsonObject? = null,
  previousConfiguration: io.vertx.core.json.JsonObject? = null): ConfigurationChange = io.vertx.config.ConfigurationChange().apply {

  if (newConfiguration != null) {
    this.newConfiguration = newConfiguration
  }

  if (previousConfiguration != null) {
    this.previousConfiguration = previousConfiguration
  }

}


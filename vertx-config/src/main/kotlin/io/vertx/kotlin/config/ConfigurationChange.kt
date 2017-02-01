package io.vertx.kotlin.config

import io.vertx.config.ConfigChange

fun ConfigurationChange(
    newConfiguration: io.vertx.core.json.JsonObject? = null,
  previousConfiguration: io.vertx.core.json.JsonObject? = null): ConfigChange = ConfigChange().apply {

  if (newConfiguration != null) {
    this.newConfiguration = newConfiguration
  }

  if (previousConfiguration != null) {
    this.previousConfiguration = previousConfiguration
  }

}


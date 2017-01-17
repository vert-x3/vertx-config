package io.vertx.kotlin.ext.configuration

import io.vertx.ext.configuration.ConfigurationChange

fun ConfigurationChange(
    newConfiguration: io.vertx.core.json.JsonObject? = null,
  previousConfiguration: io.vertx.core.json.JsonObject? = null): ConfigurationChange = io.vertx.ext.configuration.ConfigurationChange().apply {

  if (newConfiguration != null) {
    this.newConfiguration = newConfiguration
  }

  if (previousConfiguration != null) {
    this.previousConfiguration = previousConfiguration
  }

}


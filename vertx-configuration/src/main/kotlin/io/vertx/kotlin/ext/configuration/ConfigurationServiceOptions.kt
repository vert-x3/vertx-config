package io.vertx.kotlin.ext.configuration

import io.vertx.ext.configuration.ConfigurationServiceOptions

fun ConfigurationServiceOptions(
    scanPeriod: Long? = null,
  stores: List<io.vertx.ext.configuration.ConfigurationStoreOptions>? = null): ConfigurationServiceOptions = io.vertx.ext.configuration.ConfigurationServiceOptions().apply {

  if (scanPeriod != null) {
    this.scanPeriod = scanPeriod
  }

  if (stores != null) {
    this.stores = stores
  }

}


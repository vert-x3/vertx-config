package io.vertx.kotlin.ext.configuration

import io.vertx.ext.configuration.ConfigurationRetrieverOptions

fun ConfigurationRetrieverOptions(
    scanPeriod: Long? = null,
  stores: List<io.vertx.ext.configuration.ConfigurationStoreOptions>? = null): ConfigurationRetrieverOptions = io.vertx.ext.configuration.ConfigurationRetrieverOptions().apply {

  if (scanPeriod != null) {
    this.scanPeriod = scanPeriod
  }

  if (stores != null) {
    this.stores = stores
  }

}


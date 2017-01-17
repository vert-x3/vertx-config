package io.vertx.kotlin.config

import io.vertx.config.ConfigurationRetrieverOptions

fun ConfigurationRetrieverOptions(
    scanPeriod: Long? = null,
  stores: List<io.vertx.config.ConfigurationStoreOptions>? = null): ConfigurationRetrieverOptions = io.vertx.config.ConfigurationRetrieverOptions().apply {

  if (scanPeriod != null) {
    this.scanPeriod = scanPeriod
  }

  if (stores != null) {
    this.stores = stores
  }

}


package io.vertx.kotlin.config

import io.vertx.config.ConfigRetrieverOptions

fun ConfigRetrieverOptions(
    scanPeriod: Long? = null,
  stores: List<io.vertx.config.ConfigStoreOptions>? = null): ConfigRetrieverOptions = io.vertx.config.ConfigRetrieverOptions().apply {

  if (scanPeriod != null) {
    this.scanPeriod = scanPeriod
  }

  if (stores != null) {
    this.stores = stores
  }

}


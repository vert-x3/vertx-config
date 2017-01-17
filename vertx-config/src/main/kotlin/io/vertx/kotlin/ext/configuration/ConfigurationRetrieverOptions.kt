package io.vertx.kotlin.ext.configuration

import io.vertx.config.ConfigurationRetrieverOptions
import io.vertx.config.ConfigurationStoreOptions

fun ConfigurationRetrieverOptions(
    scanPeriod: Long? = null,
  stores: List<ConfigurationStoreOptions>? = null): ConfigurationRetrieverOptions = ConfigurationRetrieverOptions().apply {

  if (scanPeriod != null) {
    this.scanPeriod = scanPeriod
  }

  if (stores != null) {
    this.stores = stores
  }

}


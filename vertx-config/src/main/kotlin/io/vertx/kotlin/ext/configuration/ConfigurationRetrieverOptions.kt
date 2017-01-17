package io.vertx.kotlin.ext.configuration

import io.vertx.config.ConfigRetrieverOptions
import io.vertx.config.ConfigStoreOptions

fun ConfigurationRetrieverOptions(
    scanPeriod: Long? = null,
  stores: List<ConfigStoreOptions>? = null): ConfigRetrieverOptions = ConfigRetrieverOptions().apply {

  if (scanPeriod != null) {
    this.scanPeriod = scanPeriod
  }

  if (stores != null) {
    this.stores = stores
  }

}


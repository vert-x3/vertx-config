/*
 * Copyright (c) 2014 Red Hat, Inc. and others
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package io.vertx.kotlin.config.vault.client

import io.vertx.config.vault.client.TokenRequest

/**
 * A function providing a DSL for building [io.vertx.config.vault.client.TokenRequest] objects.
 *
 * The token request structure.
 *
 * @param displayName
 * @param id
 * @param meta
 * @param noDefaultPolicy
 * @param noParent
 * @param numUses
 * @param policies
 * @param role
 * @param ttl
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.config.vault.client.TokenRequest original] using Vert.x codegen.
 */
fun TokenRequest(
  displayName: String? = null,
  id: String? = null,
  meta: Map<String, String>? = null,
  noDefaultPolicy: Boolean? = null,
  noParent: Boolean? = null,
  numUses: Long? = null,
  policies: Iterable<String>? = null,
  role: String? = null,
  ttl: String? = null): TokenRequest = io.vertx.config.vault.client.TokenRequest().apply {

  if (displayName != null) {
    this.setDisplayName(displayName)
  }
  if (id != null) {
    this.setId(id)
  }
  if (meta != null) {
    this.setMeta(meta)
  }
  if (noDefaultPolicy != null) {
    this.setNoDefaultPolicy(noDefaultPolicy)
  }
  if (noParent != null) {
    this.setNoParent(noParent)
  }
  if (numUses != null) {
    this.setNumUses(numUses)
  }
  if (policies != null) {
    this.setPolicies(policies.toList())
  }
  if (role != null) {
    this.setRole(role)
  }
  if (ttl != null) {
    this.setTTL(ttl)
  }
}


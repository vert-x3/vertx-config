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

package io.vertx.config.kubernetes;

import java.io.IOException;
import java.io.InputStream;

/**
 * Utility methods for Kubernetes.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class KubernetesUtils {


  public static final String OPENSHIFT_KUBERNETES_TOKEN_FILE = "/var/run/secrets/kubernetes.io/serviceaccount/token";

  private static void closeQuietly(InputStream is) {
    if (is != null) {
      try {
        is.close();
      } catch (IOException e) {
        // Ignore it.
      }
    }
  }
}


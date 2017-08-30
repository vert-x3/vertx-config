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


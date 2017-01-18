package io.vertx.config.verticle;

import io.vertx.core.AbstractVerticle;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class MyVerticle extends AbstractVerticle {

  public static String mark;

  @Override
  public void start() throws Exception {
    mark = config().getString("mark");
  }
}

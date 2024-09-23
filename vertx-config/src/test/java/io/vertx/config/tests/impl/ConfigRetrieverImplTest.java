package io.vertx.config.tests.impl;

import io.vertx.config.impl.ConfigRetrieverImpl;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ConfigRetrieverImplTest {


  @Test
  public void testFormatExtractionFromPath() {
    String path = "config.json";
    Assertions.assertThat(ConfigRetrieverImpl.extractFormatFromFileExtension(path)).isEqualTo("json");
    path = "config.Json";
    assertThat(ConfigRetrieverImpl.extractFormatFromFileExtension(path)).isEqualTo("json");
    path = "." + File.separator + "config.json";
    assertThat(ConfigRetrieverImpl.extractFormatFromFileExtension(path)).isEqualTo("json");
    path = ".config.json";
    assertThat(ConfigRetrieverImpl.extractFormatFromFileExtension(path)).isEqualTo("json");
    path = "." + File.separator + ".config" + File.separator + "config.json";
    assertThat(ConfigRetrieverImpl.extractFormatFromFileExtension(path)).isEqualTo("json");

    path = "config.yaml";
    assertThat(ConfigRetrieverImpl.extractFormatFromFileExtension(path)).isEqualTo("yaml");
    path = "config.yml";
    assertThat(ConfigRetrieverImpl.extractFormatFromFileExtension(path)).isEqualTo("yaml");

    path = "config.";
    assertThat(ConfigRetrieverImpl.extractFormatFromFileExtension(path)).isEqualTo("json");
    path = "";
    assertThat(ConfigRetrieverImpl.extractFormatFromFileExtension(path)).isEqualTo("json");
    path = " ";
    assertThat(ConfigRetrieverImpl.extractFormatFromFileExtension(path)).isEqualTo("json");
    path = " . ";
    assertThat(ConfigRetrieverImpl.extractFormatFromFileExtension(path)).isEqualTo("json");
  }

}

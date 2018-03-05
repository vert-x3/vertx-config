package io.vertx.config;

import io.vertx.core.json.JsonObject;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigStoreOptionsTest {

  @Test
  public void testDefaultConstructor() {;
    final ConfigStoreOptions options = new ConfigStoreOptions();
    assertThat(options.getConfig()).isEqualTo(null);
    assertThat(options.getFormat()).isEqualTo(null);
    assertThat(options.getType()).isEqualTo(null);
    assertThat(options.isOptional()).isEqualTo(false);
  }

  @Test
  public void testConstructorWithEmptyJsonConfiguration() {
    final ConfigStoreOptions options = new ConfigStoreOptions(new JsonObject());
    assertThat(options.getConfig()).isEqualTo(null);
    assertThat(options.getFormat()).isEqualTo("json");
    assertThat(options.getType()).isEqualTo(null);
    assertThat(options.isOptional()).isEqualTo(false);
  }
}

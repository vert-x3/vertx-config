package io.vertx.config.tests;

import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

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

  @Test
  public void testCopyConstructorWithDefaults() {
    ConfigStoreOptions options = new ConfigStoreOptions();
    ConfigStoreOptions copy = new ConfigStoreOptions(options);
    assertThat(copy.isOptional()).isFalse();
    assertThat(copy.getConfig()).isNull();
    assertThat(copy.getFormat()).isEqualTo(options.getFormat()).isNull();
    assertThat(copy.getType()).isEqualTo(options.getType()).isNull();
  }

  @Test
  public void testCopyConstructorWithContent() {
    ConfigStoreOptions options = new ConfigStoreOptions();
    options.setOptional(true)
      .setType("file")
      .setConfig(new JsonObject().put("key", "value"))
      .setFormat("yaml");
    ConfigStoreOptions copy = new ConfigStoreOptions(options);
    assertThat(copy.isOptional()).isTrue();
    assertThat(copy.getConfig()).containsExactly(entry("key", "value"));
    assertThat(copy.getFormat()).isEqualTo(options.getFormat()).isEqualTo("yaml");
    assertThat(copy.getType()).isEqualTo(options.getType()).isEqualTo("file");
  }
}

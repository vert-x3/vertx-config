package io.vertx.config.vault.client;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Map;

/**
 * Represents Auth result.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Auth {

  @JsonProperty("accessor")
  private String accessor;

  @JsonProperty("lease_duration")
  private long leaseDuration;

  @JsonProperty("renewable")
  private boolean renewable;

  private JsonObject metadata;

  @JsonProperty("client_token")
  private String token;

  @JsonProperty("policies")
  private List<String> policies;

  public long getLeaseDuration() {
    return leaseDuration;
  }

  public boolean isRenewable() {
    return renewable;
  }

  public JsonObject getMetadata() {
    return metadata;
  }

  @JsonProperty("metadata")
  private void setMetadata(Map<String, Object> meta) {
    this.metadata = new JsonObject(meta);
  }

  public String getToken() {
    return token;
  }

  public List<String> getPolicies() {
    return policies;
  }

  public String getApplicationId() {
    return getMetadata() != null ? getMetadata().getString("app-id") : null;
  }

  public String getUserId() {
    return getMetadata() != null ? getMetadata().getString("user-id") : null;
  }

  public String getUsername() {
    return getMetadata() != null ? getMetadata().getString("username") : null;
  }

  public String getAccessor() {
    return accessor;
  }

  @Override
  public String toString() {
    return "Auth{" + "accessor='" + accessor + '\'' +
      ", leaseDuration=" + leaseDuration +
      ", renewable=" + renewable +
      ", metadata=" + metadata +
      ", token='" + token + '\'' +
      ", policies=" + policies +
      '}';
  }
}

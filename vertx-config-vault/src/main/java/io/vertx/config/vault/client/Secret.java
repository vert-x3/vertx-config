package io.vertx.config.vault.client;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;

import java.util.Map;

/**
 * Represent Secret result.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Secret {

  @JsonProperty("lease_id")
  private String leaseId;

  @JsonProperty("renewable")
  private boolean renewable;

  @JsonProperty("lease_duration")
  private long leaseDuration;

  @JsonProperty("request_id")
  private String requestId;

  private JsonObject data;

  public String getLeaseId() {
    return leaseId;
  }

  public boolean isRenewable() {
    return renewable;
  }

  public long getLeaseDuration() {
    return leaseDuration;
  }

  public JsonObject getData() {
    return data;
  }

  @JsonProperty("data")
  private void setData(Map<String, Object> data) {
    this.data = new JsonObject(data);
  }

  public String getRequestId() {
    return requestId;
  }
}

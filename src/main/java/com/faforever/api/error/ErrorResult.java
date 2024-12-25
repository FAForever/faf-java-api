package com.faforever.api.error;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

@Data
@RequiredArgsConstructor
@AllArgsConstructor(onConstructor_={@JsonCreator(mode = Mode.DELEGATING)})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResult {
  @JsonProperty("status")
  private final String httpStatusCode;
  private final String title;
  private final String detail;
  @JsonProperty("code")
  private String appCode;
  private Meta meta;

  public static Optional<Meta> createMeta(Object[] args, Map<String, String> additionalInfo) {
    if ((args == null || args.length == 0) && (additionalInfo == null || additionalInfo.isEmpty())) {
      return Optional.empty();
    }
    return Optional.of(new Meta(args, additionalInfo));
  }

  @Data
  @RequiredArgsConstructor
  @AllArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Meta {
    private Object[] args;
    private Map<String, String> additionalInfo;
  }
}

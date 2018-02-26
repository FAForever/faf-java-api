package com.faforever.api.mautic;

import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Data
public class MauticErrorResponse {
  private List<Error> errors;

  @Data
  @ToString
  public static class Error {
    private int code;
    private String message;
    private Map<String, Object> details;
  }
}

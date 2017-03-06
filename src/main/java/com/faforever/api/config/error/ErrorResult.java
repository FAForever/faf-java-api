package com.faforever.api.config.error;

import lombok.Data;

@Data
public class ErrorResult {
  private final String title;
  private final String detail;
}

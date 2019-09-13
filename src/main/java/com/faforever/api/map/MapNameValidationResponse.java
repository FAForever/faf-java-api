package com.faforever.api.map;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MapNameValidationResponse {
  String displayName;
  int nextVersion;
  String folderName;
  FileNames fileNames;

  @Value
  @Builder
  static class FileNames {
    String scmap;
    String saveLua;
    String scenarioLua;
    String scriptLua;
  }
}

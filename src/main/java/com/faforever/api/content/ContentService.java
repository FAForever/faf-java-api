package com.faforever.api.content;

import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class ContentService {

  public Path createTempDir() {
    return com.google.common.io.Files.createTempDir().toPath();
  }
}

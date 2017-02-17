package com.faforever.api.content;

import com.google.common.io.Files;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class ContentService {

  public Path createTempDir() {
    return Files.createTempDir().toPath();
  }
}

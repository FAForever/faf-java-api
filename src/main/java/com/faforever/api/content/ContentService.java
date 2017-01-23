package com.faforever.api.content;

import com.faforever.api.config.FafApiProperties;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.nio.file.Path;

@Service
public class ContentService {

  private final FafApiProperties fafApiProperties;

  @Inject
  public ContentService(FafApiProperties fafApiProperties) {
    this.fafApiProperties = fafApiProperties;
  }

  public Path createTempDir() {
    return com.google.common.io.Files.createTempDir().toPath();
  }
}

package com.faforever.api.utils;

import java.io.InputStream;
import java.nio.file.Path;

public final class FileHandlingHelper {

  private FileHandlingHelper() {
    throw new AssertionError("Cannot instantiate class");
  }

  public static InputStream loadResourceAsStream(String filePath) {
    return FileHandlingHelper.class.getResourceAsStream(filePath);
  }

  public static InputStream loadResourceAsStream(Path filePath) {
    return FileHandlingHelper.class.getResourceAsStream(filePath.toString());
  }
}

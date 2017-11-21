package com.faforever.api.utils;

import org.apache.commons.lang3.StringUtils;

import java.nio.file.Paths;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Locale;

public final class FileNameUtil {
  private FileNameUtil() {
    // Utility class
  }

  public static String normalizeFileName(String originalFileName) {
    return StringUtils.strip(
      Normalizer.normalize(
        Paths.get(originalFileName.toLowerCase(Locale.US)).getFileName().toString()
          .replaceAll("[/\\\\ :*?<>|\"]", "_"),
        Form.NFKC),
      "_");
  }
}

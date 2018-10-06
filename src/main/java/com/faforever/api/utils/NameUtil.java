package com.faforever.api.utils;

import org.apache.commons.lang3.StringUtils;

import java.nio.file.Paths;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Locale;

public final class NameUtil {
  private NameUtil() {
    // Utility class
  }

  /**
   * Returns true if the string only consists of all printable ASCII characters (see ASCII table from SP to ~ [SP being
   * space])
   */
  public static boolean isPrintableAsciiString(String name) {
    return !name.matches(".*[^\\x20-\\x7E]+.*");
  }

  public static String normalizeFileName(String originalFileName) {
    return StringUtils.strip(
      Normalizer.normalize(
        Paths.get(originalFileName
          .replaceAll("[/\\\\ :*?<>|\"]", "_")
          .toLowerCase(Locale.US))
          .getFileName()
          .toString(),
        Form.NFKC),
      "_");
  }
}

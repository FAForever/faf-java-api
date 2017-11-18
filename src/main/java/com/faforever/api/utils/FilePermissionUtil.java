package com.faforever.api.utils;


import lombok.SneakyThrows;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

public final class FilePermissionUtil {
  private static final String FILE_PERMISSION_POSIX = "posix";

  private FilePermissionUtil() {
    // Utility class
  }

  @SneakyThrows
  public static void setDefaultFilePermission(Path path) {
    if (!FileSystems.getDefault().supportedFileAttributeViews().contains(FILE_PERMISSION_POSIX)) {
      return;
    }

    Set<PosixFilePermission> permissions = new HashSet<>();
    permissions.add(PosixFilePermission.OWNER_READ);
    permissions.add(PosixFilePermission.OWNER_WRITE);
    permissions.add(PosixFilePermission.GROUP_READ);
    permissions.add(PosixFilePermission.GROUP_WRITE);
    permissions.add(PosixFilePermission.OTHERS_READ);

    Files.setPosixFilePermissions(path, permissions);
  }
}

package com.faforever.api.utils;


import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashSet;
import java.util.Set;

@UtilityClass
public final class FilePermissionUtil {

  private static final FileAttribute<Set<PosixFilePermission>> DIRECTORY_PERMISSION_FILE_ATTRIBUTES
    = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxrwxr-x"));

  private static final String FILE_PERMISSION_POSIX = "posix";

  @SneakyThrows
  public void setDefaultFilePermission(Path path) {
    if (!FileSystems.getDefault().supportedFileAttributeViews().contains(FILE_PERMISSION_POSIX)) {
      return;
    }

    Set<PosixFilePermission> permissions = new HashSet<>();
    permissions.add(PosixFilePermission.OWNER_READ);
    permissions.add(PosixFilePermission.OWNER_WRITE);
    permissions.add(PosixFilePermission.GROUP_READ);
    permissions.add(PosixFilePermission.GROUP_WRITE);
    permissions.add(PosixFilePermission.OTHERS_READ);

    if (Files.isDirectory(path)) {
      permissions.add(PosixFilePermission.OWNER_EXECUTE);
      permissions.add(PosixFilePermission.GROUP_EXECUTE);
      permissions.add(PosixFilePermission.OTHERS_EXECUTE);
    }
    Files.setPosixFilePermissions(path, permissions);
  }

  public FileAttribute<?>[] directoryPermissionFileAttributes() {
    if (!FileSystems.getDefault().supportedFileAttributeViews().contains(FILE_PERMISSION_POSIX)) {
      return new FileAttribute[0];
    }

    return new FileAttribute[]{
      DIRECTORY_PERMISSION_FILE_ATTRIBUTES
    };
  }
}

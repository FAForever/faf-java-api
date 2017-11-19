package com.faforever.api.avatar;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.Avatar;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.utils.FilePermissionUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
@Slf4j
public class AvatarService {
  private final AvatarRepository avatarRepository;
  private final FafApiProperties properties;

  public AvatarService(AvatarRepository avatarRepository, FafApiProperties properties) {
    this.avatarRepository = avatarRepository;
    this.properties = properties;
  }

  @SneakyThrows
  @Transactional
  public void processUploadedAvatar(String tooltip, String originalFilename, InputStream imageDataInputStream, long avatarImageFileSize) {
    String url = properties.getAvatar().getDownloadUrlBase() + originalFilename;
    validateImageFile(originalFilename, avatarImageFileSize);

    avatarRepository.findByUrl(url).ifPresent(avatar -> {
      throw new ApiException(new Error(ErrorCode.AVATAR_NAME_CONFLICT, originalFilename));
    });
    final Path imageTargetPath = properties.getAvatar().getTargetDirectory().resolve(originalFilename);
    Files.copy(imageDataInputStream, imageTargetPath, StandardCopyOption.REPLACE_EXISTING);
    FilePermissionUtil.setDefaultFilePermission(imageTargetPath);
    final Avatar avatar = new Avatar()
      .setTooltip(tooltip)
      .setUrl(url);
    try {
      avatarRepository.save(avatar);
    } catch (DataAccessException e) {
      try {
        Files.delete(imageTargetPath);
      } catch (IOException ioException) {
        log.warn("Could not delete file " + imageTargetPath, ioException);
      }
      throw e;
    }
  }

  @SneakyThrows
  @Transactional
  public void deleteAvatar(Integer avatarId) {
    final Avatar avatar = avatarRepository.getOne(avatarId);
    if (!avatar.getAssignments().isEmpty()) {
      throw new ApiException(new Error(ErrorCode.AVATAR_IN_USE));
    }
    final String downloadUrlBase = properties.getAvatar().getDownloadUrlBase();
    final String fileName = avatar.getUrl().replace(downloadUrlBase, "");
    final Path avatarImageFilePath = properties.getAvatar().getTargetDirectory().resolve(fileName);
    Files.deleteIfExists(avatarImageFilePath);
    avatarRepository.delete(avatar);
  }

  private void validateImageFile(String originalFilename, long avatarImageFileSize) {
    final int maxFileNameLength = properties.getAvatar().getMaxNameLength();
    final Integer maxFileSizeBytes = properties.getAvatar().getMaxSizeBytes();
    String extension = com.google.common.io.Files.getFileExtension(originalFilename);

    if (avatarImageFileSize > maxFileSizeBytes) {
      throw new ApiException(new Error(ErrorCode.FILE_SIZE_EXCEEDED, maxFileSizeBytes, avatarImageFileSize));
    }
    if (properties.getAvatar().getAllowedFileExtensions().stream().noneMatch(extension::equals)) {
      throw new ApiException(new Error(ErrorCode.UPLOAD_INVALID_FILE_EXTENSIONS, String.join(", ", properties.getAvatar().getAllowedFileExtensions())));
    }
    if (originalFilename.length() > maxFileNameLength) {
      throw new ApiException(new Error(ErrorCode.FILE_NAME_TOO_LONG, maxFileNameLength, originalFilename.length()));
    }
  }
}

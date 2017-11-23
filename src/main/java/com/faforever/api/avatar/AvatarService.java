package com.faforever.api.avatar;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.Avatar;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.error.NotFoundApiException;
import com.faforever.api.utils.FileNameUtil;
import com.faforever.api.utils.FilePermissionUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

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
  public void processUploadedAvatar(AvatarMetadata avatarMetadata, String originalFilename, InputStream imageDataInputStream, long avatarImageFileSize) {
    final String normalizedAvatarFileName = FileNameUtil.normalizeFileName(originalFilename);
    String url = String.format(properties.getAvatar().getDownloadUrlFormat(), normalizedAvatarFileName);
    validateImageFile(originalFilename, avatarImageFileSize);
    final Path imageTargetPath = properties.getAvatar().getTargetDirectory().resolve(normalizedAvatarFileName);

    avatarRepository.findOneByUrl(url).ifPresent(avatar -> {
      throw new ApiException(new Error(ErrorCode.AVATAR_NAME_CONFLICT, normalizedAvatarFileName));
    });
    if (Files.exists(imageTargetPath)) {
      throw new ApiException(new Error(ErrorCode.AVATAR_NAME_CONFLICT, normalizedAvatarFileName));
    }

    Files.createDirectories(imageTargetPath.getParent());
    Files.copy(imageDataInputStream, imageTargetPath);
    FilePermissionUtil.setDefaultFilePermission(imageTargetPath);

    try {
      checkImageDimensions(imageTargetPath);
      final Avatar avatar = new Avatar()
        .setTooltip(avatarMetadata.getName())
        .setUrl(url);
      avatarRepository.save(avatar);
    } catch (IOException | DataAccessException e) {
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
    final Avatar avatar = avatarRepository.findById(avatarId).orElseThrow(() -> new NotFoundApiException(new Error(ErrorCode.ENTITY_NOT_FOUND, avatarId)));
    if (!avatar.getAssignments().isEmpty()) {
      throw new ApiException(new Error(ErrorCode.AVATAR_IN_USE, avatarId));
    }
    // TODO: 21.11.2017 !!!!!!!!!!!! HACK TO GET FILENAME FROM URL..... !!!!!!!!!!!!!!!
    final String fileName = Paths.get(URI.create(avatar.getUrl()).getPath()).getFileName().toString();
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
    if (!properties.getAvatar().getAllowedExtensions().contains(extension)) {
      throw new ApiException(new Error(ErrorCode.UPLOAD_INVALID_FILE_EXTENSIONS, properties.getAvatar().getAllowedExtensions()));
    }
    if (originalFilename.length() > maxFileNameLength) {
      throw new ApiException(new Error(ErrorCode.FILE_NAME_TOO_LONG, maxFileNameLength, originalFilename.length()));
    }
  }

  private void checkImageDimensions(Path imageTargetPath) throws IOException {
    final String fileExtension = com.google.common.io.Files.getFileExtension(imageTargetPath.getFileName().toString());
    final Iterator<ImageReader> imageReadersBySuffix = ImageIO.getImageReadersBySuffix(fileExtension);
    if (imageReadersBySuffix.hasNext()) {
      final ImageReader imageReader = imageReadersBySuffix.next();
      try {
        imageReader.setInput(new FileImageInputStream(imageTargetPath.toFile()));

        final int width = imageReader.getWidth(imageReader.getMinIndex());
        final int height = imageReader.getHeight(imageReader.getMinIndex());
        final int heightLimit = properties.getAvatar().getImageHeight();
        final int widthLimit = properties.getAvatar().getImageWidth();

        if (width != widthLimit || height != heightLimit) {
          throw new ApiException(new Error(ErrorCode.INVALID_AVATAR_DIMENSION, widthLimit, heightLimit, width, height));
        }
      } finally {
        imageReader.dispose();
      }
    }
  }
}

package com.faforever.api.avatar;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.Avatar;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.error.NotFoundApiException;
import com.faforever.api.error.ProgrammingError;
import com.faforever.api.security.Audit;
import com.faforever.api.utils.FileNameUtil;
import com.faforever.api.utils.FilePermissionUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
  @Audit(messageTemplate = "Avatar [''{0}'' - ''{1}''] created.", expressions = {"${avatarMetadata.name}", "${originalFilename}"})
  public void createAvatar(AvatarMetadata avatarMetadata, String originalFilename, InputStream imageDataInputStream, long avatarImageFileSize) {
    final Avatar avatarToCreate = new Avatar();
    final String normalizedAvatarFileName = FileNameUtil.normalizeFileName(originalFilename);
    String url = String.format(properties.getAvatar().getDownloadUrlFormat(), normalizedAvatarFileName);
    avatarRepository.findOneByUrl(url).ifPresent(existingAvatar -> {
      throw new ApiException(new Error(ErrorCode.AVATAR_NAME_CONFLICT, normalizedAvatarFileName));
    });
    avatarToCreate.setTooltip(avatarMetadata.getName())
      .setUrl(url);

    final InputStream markSupportedImageInputStream = getMarkSupportedInputStream(imageDataInputStream);
    validateImageFile(originalFilename, avatarImageFileSize);
    checkImageDimensions(markSupportedImageInputStream, normalizedAvatarFileName);
    final Path imageTargetPath = properties.getAvatar().getTargetDirectory().resolve(normalizedAvatarFileName);

    avatarRepository.save(avatarToCreate);
    copyAvatarFile(markSupportedImageInputStream, imageTargetPath, false);
  }

  @SneakyThrows
  @Transactional
  @Audit(messageTemplate = "Avatar ''{0}'' updated with [''{1}'' - ''{2}''].", expressions = {"${avatarId}", "${avatarMetadata.name}", "${originalFilename}"})
  public void updateAvatar(Integer avatarId, AvatarMetadata avatarMetadata, String originalFilename, InputStream imageDataInputStream, long avatarImageFileSize) {
    final Avatar existingAvatar = getExistingAvatar(avatarId);
    final String normalizedAvatarFileName = getFileNameFromUrl(existingAvatar.getUrl());
    existingAvatar.setTooltip(avatarMetadata.getName());

    final InputStream markSupportedImageInputStream = getMarkSupportedInputStream(imageDataInputStream);
    validateImageFile(originalFilename, avatarImageFileSize);
    checkImageDimensions(markSupportedImageInputStream, originalFilename);
    final Path imageTargetPath = properties.getAvatar().getTargetDirectory().resolve(normalizedAvatarFileName);

    avatarRepository.save(existingAvatar);
    copyAvatarFile(markSupportedImageInputStream, imageTargetPath, true);
  }

  @SneakyThrows
  @Transactional
  @Audit(messageTemplate = "Avatar ''{0}'' deleted.", expressions = "${avatarId}")
  public void deleteAvatar(Integer avatarId) {
    final Avatar avatar = getExistingAvatar(avatarId);
    if (!avatar.getAssignments().isEmpty()) {
      throw new ApiException(new Error(ErrorCode.AVATAR_IN_USE, avatarId));
    }
    // TODO: 21.11.2017 !!!!!!!!!!!! HACK TO GET FILENAME FROM URL..... !!!!!!!!!!!!!!!
    final String fileName = getFileNameFromUrl(avatar.getUrl());
    final Path avatarImageFilePath = properties.getAvatar().getTargetDirectory().resolve(fileName);
    Files.deleteIfExists(avatarImageFilePath);
    avatarRepository.delete(avatar);
  }

  @NotNull
  private String getFileNameFromUrl(@NotNull String avatarUrl) {
    return Paths.get(URI.create(avatarUrl).getPath()).getFileName().toString();
  }

  private void copyAvatarFile(InputStream imageDataInputStream, Path imageTargetPath, boolean overwrite) throws IOException {
    CopyOption[] copyOptions = overwrite ? new CopyOption[]{StandardCopyOption.REPLACE_EXISTING} : new CopyOption[0];
    if (!overwrite && Files.exists(imageTargetPath)) {
      throw new ApiException(new Error(ErrorCode.AVATAR_NAME_CONFLICT, imageTargetPath.getFileName().toString()));
    }
    Files.createDirectories(imageTargetPath.getParent());
    Files.copy(imageDataInputStream, imageTargetPath, copyOptions);
    FilePermissionUtil.setDefaultFilePermission(imageTargetPath);
  }

  @NotNull
  private Avatar getExistingAvatar(Integer avatarId) {
    return avatarRepository.findById(avatarId).orElseThrow(() -> new NotFoundApiException(new Error(ErrorCode.ENTITY_NOT_FOUND, avatarId)));
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

  private InputStream getMarkSupportedInputStream(InputStream originalStream) {
    if (originalStream.markSupported()) {
      return originalStream;
    } else {
      //todo Spring returns inputstream with mark support but in case it won't we have to make one
      throw new ProgrammingError("Mark not supported");
    }
  }

  private void checkImageDimensions(InputStream imageInputStream, String imageFileName) throws IOException {
    imageInputStream.mark(4096);
    final Dimension imageDimensions = readImageDimensions(imageInputStream, imageFileName);
    imageInputStream.reset();

    final int heightLimit = properties.getAvatar().getImageHeight();
    final int widthLimit = properties.getAvatar().getImageWidth();

    if (imageDimensions.width != widthLimit || imageDimensions.height != heightLimit) {
      throw new ApiException(new Error(ErrorCode.INVALID_AVATAR_DIMENSION, widthLimit, heightLimit, imageDimensions.width, imageDimensions.height));
    }
  }

  private Dimension readImageDimensions(InputStream imageInputStream, String imageFileName) throws IOException {
    final String fileExtension = com.google.common.io.Files.getFileExtension(imageFileName);
    final Iterator<ImageReader> imageReadersBySuffix = ImageIO.getImageReadersBySuffix(fileExtension);
    if (imageReadersBySuffix.hasNext()) {
      final ImageReader imageReader = imageReadersBySuffix.next();
      try {
        imageReader.setInput(new MemoryCacheImageInputStream(imageInputStream));

        final int width = imageReader.getWidth(imageReader.getMinIndex());
        final int height = imageReader.getHeight(imageReader.getMinIndex());

        return new Dimension(width, height);
      } finally {
        imageReader.dispose();
      }
    } else {
      throw new ProgrammingError("Unsupported image format. Could not read dimensions.");
    }
  }
}

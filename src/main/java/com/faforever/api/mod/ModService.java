package com.faforever.api.mod;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.Mod;
import com.faforever.api.data.domain.ModType;
import com.faforever.api.data.domain.ModVersion;
import com.faforever.api.data.domain.Player;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import com.faforever.commons.mod.ModReader;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
@Slf4j
public class ModService {

  /** Legacy path prefix put in front of every mod file. This should be eliminated ASAP. */
  public static final String MOD_PATH_PREFIX = "mods/";
  private final FafApiProperties properties;
  private final ModRepository modRepository;
  private final ModVersionRepository modVersionRepository;

  public ModService(FafApiProperties properties, ModRepository modRepository, ModVersionRepository modVersionRepository) {
    this.properties = properties;
    this.modRepository = modRepository;
    this.modVersionRepository = modVersionRepository;
  }

  @SneakyThrows
  @Transactional
  public void processUploadedMod(Path uploadedFile, Player uploader) {
    log.debug("Player '{}' uploaded a mod", uploader);
    ModReader modReader = new ModReader();
    com.faforever.commons.mod.Mod modInfo = modReader.readZip(uploadedFile);
    validateModInfo(modInfo);

    log.debug("Mod uploaded by user '{}' is valid: {}", uploader, modInfo);

    String displayName = modInfo.getName().trim();
    short version = (short) Integer.parseInt(modInfo.getVersion().toString());

    if (!canUploadMod(displayName, uploader)) {
      throw new ApiException(new Error(ErrorCode.MOD_NOT_ORIGINAL_AUTHOR));
    }

    if (modExists(displayName, version)) {
      throw new ApiException(new Error(ErrorCode.MOD_VERSION_EXISTS));
    }

    String zipFileName = generateZipFileName(displayName, version);
    Path targetPath = properties.getMod().getTargetDirectory().resolve(zipFileName);
    if (Files.exists(targetPath)) {
      throw new ApiException(new Error(ErrorCode.MOD_NAME_CONFLICT, zipFileName));
    }

    Optional<Path> thumbnailPath = extractThumbnail(uploadedFile, version, displayName, modInfo.getIcon());

    log.debug("Moving uploaded mod '{}' to: {}", modInfo.getName(), targetPath);
    Files.createDirectories(targetPath.getParent());
    Files.move(uploadedFile, targetPath);

    try {
      store(modInfo, thumbnailPath, uploader, zipFileName);
    } catch (Exception exception) {
      try {
        Files.delete(targetPath);
      } catch (IOException ioException) {
        log.warn("Could not delete file " + targetPath, ioException);
      }
      throw exception;
    }
  }

  private boolean modExists(String displayName, short version) {
    ModVersion probe = new ModVersion()
        .setVersion(version)
        .setMod(new Mod()
            .setDisplayName(displayName)
        );
    return modVersionRepository.exists(Example.of(probe, ExampleMatcher.matching().withIgnoreCase()));
  }

  private boolean canUploadMod(String displayName, Player uploader) {
    return !modRepository.existsByDisplayNameIgnoreCaseAndUploaderIsNot(displayName, uploader);
  }

  private void validateModInfo(com.faforever.commons.mod.Mod modInfo) {
    List<Error> errors = new ArrayList<>();
    String name = modInfo.getName();
    if (name == null) {
      errors.add(new Error(ErrorCode.MOD_NAME_MISSING));
    } else if (name.length() > properties.getMod().getMaxNameLength()) {
      errors.add(new Error(ErrorCode.MOD_NAME_TOO_LONG));
    }
    if (modInfo.getUid() == null) {
      errors.add(new Error(ErrorCode.MOD_UID_MISSING));
    }
    if (modInfo.getVersion() == null) {
      errors.add(new Error(ErrorCode.MOD_VERSION_MISSING));
    }
    if (modInfo.getDescription() == null) {
      errors.add(new Error(ErrorCode.MOD_DESCRIPTION_MISSING));
    }
    if (modInfo.getAuthor() == null) {
      errors.add(new Error(ErrorCode.MOD_AUTHOR_MISSING));
    }

    if (!errors.isEmpty()) {
      throw new ApiException(errors.toArray(new Error[errors.size()]));
    }
  }

  @SneakyThrows
  @Nullable
  private Optional<Path> extractThumbnail(Path modZipFile, short version, String displayName, String icon) {
    if (icon == null) {
      return Optional.empty();
    }

    try (ZipFile zipFile = new ZipFile(modZipFile.toFile(), ZipFile.OPEN_READ)) {
      ZipEntry entry = zipFile.getEntry(icon.replace("/mods/", ""));
      if (entry == null) {
        return Optional.empty();
      }

      String thumbnailFileName = generateThumbnailFileName(displayName, version);
      Path targetPath = properties.getMod().getThumbnailTargetDirectory().resolve(thumbnailFileName);

      log.debug("Extracting thumbnail of mod '{}' to: {}", displayName, targetPath);
      Files.createDirectories(targetPath.getParent());
      try (InputStream inputStream = new BufferedInputStream(zipFile.getInputStream(entry))) {
        Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
      }

      return Optional.of(targetPath);
    }
  }

  private String generateThumbnailFileName(String name, short version) {
    return generateFolderName(name, version) + ".png";
  }

  private String generateZipFileName(String displayName, short version) {
    return generateFolderName(displayName, version) + ".zip";
  }

  private String generateFolderName(String displayName, short version) {
    return String.format("%s.v%04d", generateFileName(displayName), version);
  }

  private String generateFileName(String displayName) {
    return Normalizer.normalize(displayName.toLowerCase(Locale.US)
            .replace("..", ".")
            .replaceAll("[/\\\\ ]", "_"),
        Form.NFKC);
  }

  private void store(com.faforever.commons.mod.Mod modInfo, Optional<Path> thumbnailPath, Player uploader, String zipFileName) {
    ModVersion modVersion = new ModVersion()
      .setUid(modInfo.getUid())
      .setType(modInfo.isUiOnly() ? ModType.UI : ModType.SIM)
      .setDescription(modInfo.getDescription())
      .setVersion((short) Integer.parseInt(modInfo.getVersion().toString()))
      .setFilename(MOD_PATH_PREFIX + zipFileName)
      .setIcon(thumbnailPath.map(path -> path.getFileName().toString()).orElse(null));

    List<ModVersion> modVersions = new ArrayList<>();
    modVersions.add(modVersion);

    Mod mod = new Mod()
      .setAuthor(modInfo.getAuthor())
      .setDisplayName(modInfo.getName())
      .setVersions(modVersions)
      .setUploader(uploader);

    modVersion.setMod(mod);

    mod = modRepository.save(mod);
    modRepository.insertModStats(mod.getDisplayName());
  }
}

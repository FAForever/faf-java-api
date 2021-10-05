package com.faforever.api.mod;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.BanDurationType;
import com.faforever.api.data.domain.BanLevel;
import com.faforever.api.data.domain.Mod;
import com.faforever.api.data.domain.ModType;
import com.faforever.api.data.domain.ModVersion;
import com.faforever.api.data.domain.Player;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.utils.FilePermissionUtil;
import com.faforever.api.utils.NameUtil;
import com.faforever.commons.io.Unzipper;
import com.faforever.commons.mod.ModReader;
import com.google.common.primitives.Ints;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.luaj.vm2.LuaValue;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.text.MessageFormat.format;

@Service
@Slf4j
@RequiredArgsConstructor
public class ModService {

  /**
   * Legacy path prefix put in front of every mod file. This should be eliminated ASAP.
   */
  public static final String MOD_PATH_PREFIX = "mods/";
  private final FafApiProperties properties;
  private final ModRepository modRepository;
  private final ModVersionRepository modVersionRepository;

  @SneakyThrows
  @Transactional
  @CacheEvict(value = {Mod.TYPE_NAME, ModVersion.TYPE_NAME}, allEntries = true)
  public void processUploadedMod(Path uploadedFile, Player uploader) {
    checkUploaderVaultBan(uploader);

    log.debug("Player '{}' uploaded a mod", uploader);

    validateZipFileSafety(uploadedFile);

    ModReader modReader = new ModReader();
    com.faforever.commons.mod.Mod modInfo = modReader.readZip(uploadedFile);
    validateModInfo(modInfo);
    validateModStructure(uploadedFile);

    log.debug("Mod uploaded by user '{}' is valid: {}", uploader, modInfo);

    String displayName = modInfo.getName().trim();
    short version = (short) Integer.parseInt(modInfo.getVersion().toString());

    if (!canUploadMod(displayName, uploader)) {
      Mod mod = modRepository.findOneByDisplayName(displayName)
        .orElseThrow(() -> new IllegalStateException("Mod could not be found"));
      throw new ApiException(new Error(ErrorCode.MOD_NOT_ORIGINAL_AUTHOR, mod.getAuthor(), displayName));
    }

    if (modExists(displayName, version)) {
      throw new ApiException(new Error(ErrorCode.MOD_VERSION_EXISTS, displayName, version));
    }

    String uuid = modInfo.getUid();
    if (modUidExists(uuid)) {
      throw new ApiException(new Error(ErrorCode.MOD_UID_EXISTS, uuid));
    }

    String zipFileName = generateZipFileName(displayName, version);
    Path targetPath = properties.getMod().getTargetDirectory().resolve(zipFileName);
    if (Files.exists(targetPath)) {
      throw new ApiException(new Error(ErrorCode.MOD_NAME_CONFLICT, zipFileName));
    }

    Optional<Path> thumbnailPath = extractThumbnail(uploadedFile, version, displayName, modInfo.getIcon());

    log.debug("Moving uploaded mod '{}' to: {}", modInfo.getName(), targetPath);
    Files.createDirectories(targetPath.getParent(), FilePermissionUtil.directoryPermissionFileAttributes());
    Files.move(uploadedFile, targetPath);
    FilePermissionUtil.setDefaultFilePermission(targetPath);

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

  /**
   * Make sure that the zip file does not contain a zip bomb or zip slip attacks
   */
  @SneakyThrows
  private void validateZipFileSafety(Path uploadedFile) {
    log.debug("Validating file safety of uploaded file {}", uploadedFile);
    Path tempDirectory = Files.createTempDirectory("validate_zip");

    try {
      // Unzipping directory already invokes the checks we want to perform
      Unzipper.from(uploadedFile)
        .to(tempDirectory)
        .unzip();

    } finally {
      log.debug("Delete unzipped files in folder {}", tempDirectory);
      FileUtils.deleteDirectory(tempDirectory.toFile());
    }
  }

  /**
   * Ensure that all files of the zip are inside at least one root folder. Otherwise the mods will overwrite each other
   * on client side.
   */
  @SneakyThrows
  private void validateModStructure(Path uploadedFile) {
    try (ZipFile zipFile = new ZipFile(uploadedFile.toFile())) {
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        ZipEntry zipEntry = entries.nextElement();

        if (!zipEntry.isDirectory() && !zipEntry.getName().contains("/")) {
          throw new ApiException(new Error(ErrorCode.MOD_STRUCTURE_INVALID));
        }
      }
    }
  }

  private boolean modExists(String displayName, short version) {
    ModVersion probe = new ModVersion()
      .setVersion(version)
      .setMod(new Mod()
        .setDisplayName(displayName)
      );

    return modVersionRepository.exists(Example.of(probe, ExampleMatcher.matching()));
  }

  private boolean modUidExists(String uuid) {
    return modVersionRepository.existsByUid(uuid);
  }

  private void checkUploaderVaultBan(Player uploader) {
    uploader.getActiveBanOf(BanLevel.VAULT)
      .ifPresent((banInfo) -> {
        String message = banInfo.getDuration() == BanDurationType.PERMANENT ?
          "You are permanently banned from uploading mods to the vault." :
          format("You are banned from uploading mods to the vault until {0}.", banInfo.getExpiresAt());
        throw HttpClientErrorException.create(message, HttpStatus.FORBIDDEN, "Upload forbidden",
          HttpHeaders.EMPTY, null, null);
      });
  }

  private boolean canUploadMod(String displayName, Player uploader) {
    return !modRepository.existsByDisplayNameAndUploaderIsNot(displayName, uploader);
  }

  private boolean nullOrNil(String value) {
    return value == null || value.equalsIgnoreCase(LuaValue.NIL.toString());
  }

  private void validateModInfo(com.faforever.commons.mod.Mod modInfo) {
    List<Error> errors = new ArrayList<>();
    String name = modInfo.getName();
    if (nullOrNil(name)) {
      errors.add(new Error(ErrorCode.MOD_NAME_MISSING));
    } else {
      if (name.length() > properties.getMod().getMaxNameLength()) {
        errors.add(new Error(ErrorCode.MOD_NAME_TOO_LONG, properties.getMod().getMaxNameLength(), name.length()));
      }
      if (name.length() < properties.getMod().getMinNameLength()) {
        errors.add(new Error(ErrorCode.MOD_NAME_TOO_SHORT, properties.getMod().getMinNameLength(), name.length()));
      }
      if (!NameUtil.isPrintableAsciiString(name)) {
        errors.add(new Error(ErrorCode.MOD_NAME_INVALID));
      }
    }
    if (nullOrNil(modInfo.getUid())) {
      errors.add(new Error(ErrorCode.MOD_UID_MISSING));
    }
    if (modInfo.getVersion() == null || nullOrNil(modInfo.getVersion().toString())) {
      errors.add(new Error(ErrorCode.MOD_VERSION_MISSING));
    }
    if (Ints.tryParse(modInfo.getVersion().toString()) == null) {
      errors.add(new Error(ErrorCode.MOD_VERSION_NOT_A_NUMBER, modInfo.getVersion().toString()));
    }
    if (nullOrNil(modInfo.getDescription())) {
      errors.add(new Error(ErrorCode.MOD_DESCRIPTION_MISSING));
    }
    if (nullOrNil(modInfo.getAuthor())) {
      errors.add(new Error(ErrorCode.MOD_AUTHOR_MISSING));
    }

    if (!errors.isEmpty()) {
      throw new ApiException(errors.toArray(new Error[0]));
    }
  }

  @SneakyThrows
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
      Files.createDirectories(targetPath.getParent(), FilePermissionUtil.directoryPermissionFileAttributes());
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
    return String.format("%s.v%04d", NameUtil.normalizeFileName(displayName), version);
  }

  private void store(com.faforever.commons.mod.Mod modInfo, Optional<Path> thumbnailPath, Player uploader, String zipFileName) {
    ModVersion modVersion = new ModVersion()
      .setUid(modInfo.getUid())
      .setType(modInfo.isUiOnly() ? ModType.UI : ModType.SIM)
      .setDescription(modInfo.getDescription())
      .setVersion((short) Integer.parseInt(modInfo.getVersion().toString()))
      .setFilename(MOD_PATH_PREFIX + zipFileName)
      .setIcon(thumbnailPath.map(path -> path.getFileName().toString()).orElse(null));

    Mod mod = modRepository.findOneByDisplayName(modInfo.getName())
      .orElse(new Mod()
        .setAuthor(modInfo.getAuthor())
        .setDisplayName(modInfo.getName())
        .setVersions(new ArrayList<>())
        .setUploader(uploader))
      .setRecommended(false);
    mod.getVersions().add(modVersion);

    modVersion.setMod(mod);

    mod = modRepository.save(mod);
    modRepository.insertModStats(mod.getDisplayName());
  }
}

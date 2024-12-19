package com.faforever.api.map;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.content.ContentService;
import com.faforever.api.content.LicenseRepository;
import com.faforever.api.data.domain.BanDurationType;
import com.faforever.api.data.domain.BanLevel;
import com.faforever.api.data.domain.License;
import com.faforever.api.data.domain.Map;
import com.faforever.api.data.domain.MapVersion;
import com.faforever.api.data.domain.Player;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.map.MapNameValidationResponse.FileNames;
import com.faforever.api.utils.FilePermissionUtil;
import com.faforever.api.utils.NameUtil;
import com.faforever.commons.io.Unzipper;
import com.faforever.commons.io.ZipBombException;
import com.faforever.commons.io.Zipper;
import com.faforever.commons.map.PreviewGenerator;
import com.google.common.annotations.VisibleForTesting;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveException;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.faforever.api.map.MapService.ScenarioMapInfo.CONFIGURATION_STANDARD_TEAMS_ARMIES;
import static com.faforever.api.map.MapService.ScenarioMapInfo.CONFIGURATION_STANDARD_TEAMS_NAME;
import static com.faforever.api.map.MapService.ScenarioMapInfo.FILE_DECLARATION_MAP;
import static com.faforever.api.map.MapService.ScenarioMapInfo.FILE_DECLARATION_SAVE;
import static com.faforever.api.map.MapService.ScenarioMapInfo.FILE_DECLARATION_SCRIPT;
import static com.faforever.api.map.MapService.ScenarioMapInfo.FILE_ENDING_MAP;
import static com.faforever.api.map.MapService.ScenarioMapInfo.FILE_ENDING_SAVE;
import static com.faforever.api.map.MapService.ScenarioMapInfo.FILE_ENDING_SCENARIO;
import static com.faforever.api.map.MapService.ScenarioMapInfo.FILE_ENDING_SCRIPT;
import static com.faforever.api.map.MapService.ScenarioMapInfo.MANDATORY_FILES;
import static com.github.nocatch.NoCatch.noCatch;
import static java.text.MessageFormat.format;

@Service
@Slf4j
@AllArgsConstructor
public class MapService {
  private static final Pattern MAP_NAME_INVALID_CHARACTER_PATTERN = Pattern.compile("[a-zA-Z0-9\\- ]+");
  private static final Pattern MAP_NAME_DOES_NOT_START_WITH_LETTER_PATTERN = Pattern.compile("^[^a-zA-Z]+");
  private static final Pattern ADAPTIVE_MAP_PATTERN = Pattern.compile("AdaptiveMap\\w=\\wtrue");
  private static final int MAP_NAME_MINUS_MAX_OCCURENCE = 3;
  private static final int MAP_NAME_MIN_LENGTH = 4;
  private static final int MAP_NAME_MAX_LENGTH = 50;
  private static final int MAP_VERSION_MIN_VALUE = 1;
  private static final int MAP_VERSION_MAX_VALUE = 9999;

  private static final String[] ADAPTIVE_REQUIRED_FILES = new String[]{
    "_options.lua",
    "_tables.lua",
  };

  private static final Charset MAP_CHARSET = StandardCharsets.ISO_8859_1;
  private static final String LEGACY_FOLDER_PREFIX = "maps/";
  private final FafApiProperties fafApiProperties;
  private final MapRepository mapRepository;
  private final LicenseRepository licenseRepository;
  private final ContentService contentService;

  public MapNameValidationResponse requestMapNameValidation(String mapName) {
    Assert.notNull(mapName, "The map name is mandatory.");

    validateMapName(mapName);
    MapNameBuilder mapNameBuilder = new MapNameBuilder(mapName);

    int nextVersion = mapRepository.findOneByDisplayName(mapNameBuilder.getDisplayName())
      .map(map -> map.getVersions().stream()
        .mapToInt(MapVersion::getVersion)
        .map(i -> i + 1)
        .max()
        .orElse(1))
      .orElse(1);

    return MapNameValidationResponse.builder()
      .displayName(mapNameBuilder.getDisplayName())
      .nextVersion(nextVersion)
      .folderName(mapNameBuilder.buildFolderName(nextVersion))
      .fileNames(
        FileNames.builder()
          .scmap(mapNameBuilder.buildFileName(FILE_ENDING_MAP))
          .scenarioLua(mapNameBuilder.buildFileName(FILE_ENDING_SCENARIO))
          .scriptLua(mapNameBuilder.buildFileName(FILE_ENDING_SCRIPT))
          .saveLua(mapNameBuilder.buildFileName(FILE_ENDING_SAVE))
          .build()
      )
      .build();
  }

  @VisibleForTesting
  void validateMapName(String mapName) {
    List<Error> errors = new ArrayList<>();

    if (!MAP_NAME_INVALID_CHARACTER_PATTERN.matcher(mapName).matches()) {
      errors.add(new Error(ErrorCode.MAP_NAME_INVALID_CHARACTER));
    }

    if (mapName.length() < MAP_NAME_MIN_LENGTH) {
      errors.add(new Error(ErrorCode.MAP_NAME_TOO_SHORT, MAP_NAME_MIN_LENGTH, mapName.length()));
    }

    if (mapName.length() > MAP_NAME_MAX_LENGTH) {
      errors.add(new Error(ErrorCode.MAP_NAME_TOO_LONG, MAP_NAME_MAX_LENGTH, mapName.length()));
    }

    if (StringUtils.countOccurrencesOf(mapName, "-") > MAP_NAME_MINUS_MAX_OCCURENCE) {
      errors.add(new Error(ErrorCode.MAP_NAME_INVALID_MINUS_OCCURENCE, MAP_NAME_MINUS_MAX_OCCURENCE));
    }

    if (MAP_NAME_DOES_NOT_START_WITH_LETTER_PATTERN.matcher(mapName).find()) {
      errors.add(new Error(ErrorCode.MAP_NAME_DOES_NOT_START_WITH_LETTER));
    }

    if (!errors.isEmpty()) {
      throw new ApiException(errors.toArray(new Error[0]));
    }
  }

  @VisibleForTesting
  void validateScenarioLua(String scenarioLua) {
    try {
      MapLuaAccessor mapLua = MapLuaAccessor.of(scenarioLua);
      MapNameBuilder mapNameBuilder = new MapNameBuilder(mapLua.getName()
        .orElseThrow(() -> ApiException.of(ErrorCode.MAP_NAME_MISSING)));
      validateScenarioLua(mapLua, mapNameBuilder);
    } catch (IOException | LuaError e) {
      throw ApiException.of(ErrorCode.PARSING_LUA_FILE_FAILED, e.getMessage());
    }
  }

  @Transactional
  @SneakyThrows
  @CacheEvict(value = {Map.TYPE_NAME, MapVersion.TYPE_NAME}, allEntries = true)
  public void uploadMap(InputStream mapDataInputStream, String mapFilename, Player author, boolean isRanked, Integer licenseId) {
    String extension = com.google.common.io.Files.getFileExtension(mapFilename);
    if (!fafApiProperties.getMap().getAllowedExtensions().contains(extension)) {
      throw ApiException.of(ErrorCode.UPLOAD_INVALID_FILE_EXTENSIONS, fafApiProperties.getMap().getAllowedExtensions());
    }

    Assert.notNull(author, "'author' must not be null");
    Assert.isTrue(mapDataInputStream.available() > 0, "'mapData' must not be empty");

    checkAuthorVaultBan(author);

    Path rootTempFolder = contentService.createTempDir();

    try (mapDataInputStream) {
      Path unzippedFileFolder = unzipToTemporaryDirectory(mapDataInputStream, rootTempFolder);
      Path mapFolder = validateMapFolderStructure(unzippedFileFolder);
      validateRequiredFiles(mapFolder, MANDATORY_FILES);

      MapLuaAccessor mapLua = parseScenarioLua(mapFolder);
      MapNameBuilder mapNameBuilder = new MapNameBuilder(mapLua.getName()
        .orElseThrow(() -> ApiException.of(ErrorCode.MAP_NAME_MISSING)));

      mapLua.isAdaptive().ifPresent(isAdaptive -> {
        if (isAdaptive) {
          validateRequiredFiles(mapFolder, ADAPTIVE_REQUIRED_FILES);
        }
      });

      validateScenarioLua(mapLua, mapNameBuilder);

      Optional<Map> existingMapOptional = validateMapMetadata(mapLua, mapNameBuilder, author);

      updateHibernateMapEntities(mapLua, existingMapOptional, author, isRanked, licenseId, mapNameBuilder);

      Path mapFolderAfterRenaming = unzippedFileFolder.resolveSibling(
        mapNameBuilder.buildFolderName(mapLua.getMapVersion$()));
      Files.move(mapFolder, mapFolderAfterRenaming);

      updateLuaFiles(mapFolder, mapFolderAfterRenaming);
      generatePreview(mapFolderAfterRenaming);

      zipMapData(mapFolderAfterRenaming, mapNameBuilder.buildFinalZipPath(mapLua.getMapVersion$()));
    } finally {
      FileSystemUtils.deleteRecursively(rootTempFolder);
    }
  }

  private void checkAuthorVaultBan(Player author) {
    author.getActiveBanOf(BanLevel.VAULT)
      .ifPresent((banInfo) -> {
        String message = banInfo.getDuration() == BanDurationType.PERMANENT ?
          "You are permanently banned from uploading maps to the vault." :
          format("You are banned from uploading maps to the vault until {0}.", banInfo.getExpiresAt());
        throw HttpClientErrorException.create(message, HttpStatus.FORBIDDEN, "Upload forbidden",
          HttpHeaders.EMPTY, null, null);
      });
  }

  private Path unzipToTemporaryDirectory(InputStream mapDataInputStream, Path rootTempFolder)
    throws IOException, ArchiveException {
    Path unzippedDirectory = Files.createDirectories(rootTempFolder.resolve("unzipped-content"));
    log.debug("Unzipping uploaded file ''{}'' to: {}", mapDataInputStream, unzippedDirectory);


    try {
      Unzipper.from(mapDataInputStream)
        .zipBombByteCountThreshold(5_000_000)
        .zipBombProtectionFactor(200)
        .to(unzippedDirectory)
        .unzip();
    } catch (ZipBombException e) {
      throw ApiException.of(ErrorCode.ZIP_BOMB_DETECTED);
    }

    return unzippedDirectory;
  }

  /**
   * @param zipContentFolder The folder containing the content of the zipped map file
   * @return the root folder of the map
   */
  private Path validateMapFolderStructure(Path zipContentFolder) throws IOException {
    Path mapFolder;

    try (Stream<Path> mapFolderStream = Files.list(zipContentFolder)) {
      mapFolder = mapFolderStream
        .filter(Files::isDirectory)
        .findFirst()
        .orElseThrow(() -> ApiException.of(ErrorCode.MAP_MISSING_MAP_FOLDER_INSIDE_ZIP));
    }

    try (Stream<Path> mapFolderStream = Files.list(zipContentFolder)) {
      if (mapFolderStream.count() != 1) {
        throw ApiException.of(ErrorCode.MAP_INVALID_ZIP);
      }
    }

    return mapFolder;
  }

  @SneakyThrows
  private void validateRequiredFiles(Path mapFolder, String[] requiredFiles) {
    try (Stream<Path> mapFileStream = Files.list(mapFolder)) {
      List<String> fileNames = mapFileStream
        .map(Path::toString)
        .toList();

      List<Error> errors = Arrays.stream(requiredFiles)
        .filter(requiredEnding -> fileNames.stream().noneMatch(fileName -> fileName.endsWith(requiredEnding)))
        .map(requiredEnding -> new Error(ErrorCode.MAP_FILE_INSIDE_ZIP_MISSING, requiredEnding))
        .toList();

      if (!errors.isEmpty()) {
        throw ApiException.of(errors);
      }
    }
  }

  private MapLuaAccessor parseScenarioLua(Path mapFolder) throws IOException {
    try (Stream<Path> mapFilesStream = Files.list(mapFolder)) {
      Path scenarioLuaPath = mapFilesStream
        .filter(myFile -> myFile.toString().endsWith(FILE_ENDING_SCENARIO))
        .findFirst()
        .orElseThrow(() -> ApiException.of(ErrorCode.MAP_SCENARIO_LUA_MISSING));
      return MapLuaAccessor.of(scenarioLuaPath);
    } catch (LuaError e) {
      throw ApiException.of(ErrorCode.PARSING_LUA_FILE_FAILED, e.getMessage());
    }
  }

  private void validateScenarioLua(MapLuaAccessor mapLua, MapNameBuilder mapNameBuilder) {
    List<Error> errors = new ArrayList<>();

    validateLuaPathVariable(mapLua, FILE_DECLARATION_MAP, mapNameBuilder, FILE_ENDING_MAP).ifPresent(errors::add);
    validateLuaPathVariable(mapLua, FILE_DECLARATION_SAVE, mapNameBuilder, FILE_ENDING_SAVE).ifPresent(errors::add);
    validateLuaPathVariable(mapLua, FILE_DECLARATION_SCRIPT, mapNameBuilder, FILE_ENDING_SCRIPT).ifPresent(errors::add);

    if (mapLua.getDescription().isEmpty()) {
      errors.add(new Error(ErrorCode.MAP_DESCRIPTION_MISSING));
    }
    if (mapLua.hasInvalidTeam()) {
      errors.add(new Error(ErrorCode.MAP_FIRST_TEAM_FFA));
    }
    if (mapLua.getType().isEmpty()) {
      errors.add(new Error(ErrorCode.MAP_TYPE_MISSING));
    }
    if (mapLua.getSize().isEmpty()) {
      errors.add(new Error(ErrorCode.MAP_SIZE_MISSING));
    }

    final OptionalInt mapVersion = mapLua.getMapVersion();
    if (mapVersion.isEmpty()) {
      errors.add(new Error(ErrorCode.MAP_VERSION_MISSING));
    } else if (!isMapVersionValidRange(mapVersion.getAsInt())) {
      errors.add(new Error(ErrorCode.MAP_VERSION_INVALID_RANGE, MAP_VERSION_MIN_VALUE, MAP_VERSION_MAX_VALUE));
    }

    if (mapLua.getNoRushRadius().isEmpty()) {
      // The game can start without it, but the GPG map editor will crash on opening such a map.
      errors.add(new Error(ErrorCode.NO_RUSH_RADIUS_MISSING));
    }

    if (!errors.isEmpty()) {
      throw ApiException.of(errors);
    }
  }

  private static boolean isMapVersionValidRange(int mapVersion) {
    return mapVersion >= MAP_VERSION_MIN_VALUE && mapVersion <= MAP_VERSION_MAX_VALUE;
  }

  private Optional<Error> validateLuaPathVariable(MapLuaAccessor mapLua, String variableName, MapNameBuilder mapNameBuilder, String fileEnding) {
    String mapFileName = mapNameBuilder.buildFileName(fileEnding);
    String mapFolderNameWithoutVersion = mapNameBuilder.buildFolderNameWithoutVersion();

    String regex = format("\\/maps\\/{0}(\\.v\\d{4})?\\/{1}",
      mapFolderNameWithoutVersion, mapFileName);

    if (!mapLua.hasVariableMatchingIgnoreCase(regex, variableName)) {
      return Optional.of(new Error(ErrorCode.MAP_SCRIPT_LINE_MISSING,
        format("{0} = ''/maps/{1}/{2}''", variableName, mapFolderNameWithoutVersion, mapFileName)));
    }

    return Optional.empty();
  }

  private Optional<Map> validateMapMetadata(MapLuaAccessor mapLua, MapNameBuilder mapNameBuilder, Player author) {
    String displayName = mapNameBuilder.getDisplayName();

    validateMapName(displayName);

    int newVersion = mapLua.getMapVersion()
      .orElseThrow(() -> ApiException.of(ErrorCode.MAP_VERSION_MISSING));

    if (Files.exists(mapNameBuilder.buildFinalZipPath(newVersion))) {
      throw ApiException.of(ErrorCode.MAP_NAME_CONFLICT, mapNameBuilder.buildFinalZipName(newVersion));
    }

    Optional<Map> existingMapOptional = mapRepository.findOneByDisplayName(displayName);
    existingMapOptional
      .ifPresent(existingMap -> {
        final Player existingMapAuthor = existingMap.getAuthor();
        if (existingMapAuthor == null || !existingMapAuthor.equals(author)) {
          throw ApiException.of(ErrorCode.MAP_NOT_ORIGINAL_AUTHOR, existingMap.getDisplayName());
        }
        if (existingMap.getVersions().stream()
          .anyMatch(mapVersion -> mapVersion.getVersion() == newVersion)) {
          throw ApiException.of(ErrorCode.MAP_VERSION_EXISTS, existingMap.getDisplayName(), newVersion);
        }
      });

    return existingMapOptional;
  }

  private Map updateHibernateMapEntities(MapLuaAccessor mapLua, Optional<Map> existingMapOptional, Player author, boolean isRanked, Integer licenseId, MapNameBuilder mapNameBuilder) {
    // the scenario lua is supposed to be validate already, thus we call the unwrapping $-methods
    String mapName = mapNameBuilder.getDisplayName();

    License newLicense = getLicenseOrDefault(licenseId);
    Map map = existingMapOptional
      .orElseGet(() ->
        new Map()
          .setDisplayName(mapName)
          .setAuthor(author)
          .setGamesPlayed(0)
          .setRecommended(false)
          .setLicense(newLicense)
      );

    if (newLicense.isLessPermissiveThan(map.getLicense())) {
      throw ApiException.of(ErrorCode.LESS_PERMISSIVE_LICENSE);
    }
    LuaValue standardTeamsConfig = mapLua.getFirstTeam$();

    map
      .setMapType(mapLua.getType$())
      .setBattleType(standardTeamsConfig.get(CONFIGURATION_STANDARD_TEAMS_NAME).tojstring());

    LuaValue size = mapLua.getSize$();
    MapVersion version = new MapVersion()
      .setDescription(mapLua.getDescription$().replaceAll("<LOC .*?>", ""))
      .setWidth(size.get(1).toint())
      .setHeight(size.get(2).toint())
      .setHidden(false)
      .setRanked(isRanked)
      .setGamesPlayed(0)
      .setMaxPlayers(standardTeamsConfig.get(CONFIGURATION_STANDARD_TEAMS_ARMIES).length())
      .setVersion(mapLua.getMapVersion$())
      .setMap(map)
      .setFilename(LEGACY_FOLDER_PREFIX + mapNameBuilder.buildFinalZipName(mapLua.getMapVersion$()));

    map.getVersions().add(version);

    // this triggers validation
    mapRepository.save(map);

    return map;
  }

  private void updateLuaFiles(Path oldFolderPath, Path newFolderPath) throws IOException {
    String oldNameFolder = "/maps/" + oldFolderPath.getFileName();
    String newNameFolder = "/maps/" + newFolderPath.getFileName();
    try (Stream<Path> mapFileStream = Files.list(newFolderPath)) {
      mapFileStream
        .filter(path -> path.toString().toLowerCase().endsWith(".lua"))
        .forEach(path -> noCatch(() -> {
          List<String> lines = Files.readAllLines(path, MAP_CHARSET).stream()
            .map(line -> line.replaceAll("(?i)" + oldNameFolder, newNameFolder))
            .toList();
          Files.write(path, lines, MAP_CHARSET);
        }));
    }
  }

  private void generatePreview(Path newMapFolder) throws IOException {
    String previewFilename = newMapFolder.getFileName() + ".png";
    generateImage(
      fafApiProperties.getMap().getDirectoryPreviewPathSmall().resolve(previewFilename),
      newMapFolder,
      fafApiProperties.getMap().getPreviewSizeSmall());

    generateImage(
      fafApiProperties.getMap().getDirectoryPreviewPathLarge().resolve(previewFilename),
      newMapFolder,
      fafApiProperties.getMap().getPreviewSizeLarge());
  }

  private void zipMapData(Path newMapFolder, Path finalZipPath) throws IOException, ArchiveException {
    Files.createDirectories(finalZipPath.getParent(), FilePermissionUtil.directoryPermissionFileAttributes());
    Zipper
      .of(newMapFolder)
      .to(finalZipPath)
      .zip();
    // TODO if possible, this should be done using umask instead
    FilePermissionUtil.setDefaultFilePermission(finalZipPath);
  }

  private void generateImage(Path target, Path baseDir, int size) throws IOException {
    BufferedImage image = PreviewGenerator.generatePreview(baseDir, size, size);
    if (target.getNameCount() > 0) {
      Files.createDirectories(target.getParent(), FilePermissionUtil.directoryPermissionFileAttributes());
    }
    ImageIO.write(image, "png", target.toFile());
  }

  static class ScenarioMapInfo {
    static final String CONFIGURATION_STANDARD_TEAMS_NAME = "name";
    static final String CONFIGURATION_STANDARD_TEAMS_ARMIES = "armies";
    static final String FILE_DECLARATION_MAP = "map";
    static final String FILE_ENDING_SCENARIO = "_scenario.lua";
    static final String FILE_ENDING_MAP = ".scmap";
    static final String FILE_DECLARATION_SAVE = "save";
    static final String FILE_ENDING_SAVE = "_save.lua";
    static final String FILE_DECLARATION_SCRIPT = "script";
    static final String FILE_ENDING_SCRIPT = "_script.lua";

    static final String[] MANDATORY_FILES = new String[]{
      FILE_ENDING_SCENARIO,
      FILE_ENDING_MAP,
      FILE_ENDING_SAVE,
      FILE_ENDING_SCRIPT,
    };
  }

  private class MapNameBuilder {
    @Getter
    private final String displayName;
    private final String normalizedDisplayName;
    private String folderName;

    private MapNameBuilder(String displayName) {
      this.displayName = displayName;
      this.normalizedDisplayName = NameUtil.normalizeWhitespaces(displayName.toLowerCase());
    }

    String buildFolderNameWithoutVersion() {
      return normalizedDisplayName;
    }

    String buildFolderName(int version) {
      if (folderName == null) {
        folderName = String.format("%s.v%04d", normalizedDisplayName, version);
      }

      return folderName;
    }

    String buildFileName(String fileEnding) {
      return normalizedDisplayName + fileEnding;
    }

    String buildFinalZipName(int version) {
      return buildFolderName(version) + ".zip";
    }

    Path buildFinalZipPath(int version) {
      return fafApiProperties.getMap().getTargetDirectory().resolve(buildFinalZipName(version));
    }
  }

  public License getLicenseOrDefault(Integer licenseId) {
    return Optional.ofNullable(licenseId)
      .flatMap(licenseRepository::findById)
      .orElseGet(() -> licenseRepository.getReferenceById(fafApiProperties.getMap().getDefaultLicenseId()));
  }
}

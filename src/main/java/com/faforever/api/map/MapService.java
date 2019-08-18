package com.faforever.api.map;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.content.ContentService;
import com.faforever.api.data.domain.Map;
import com.faforever.api.data.domain.MapVersion;
import com.faforever.api.data.domain.Player;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.error.ProgrammingError;
import com.faforever.api.utils.FilePermissionUtil;
import com.faforever.api.utils.NameUtil;
import com.faforever.commons.io.Unzipper;
import com.faforever.commons.io.Zipper;
import com.faforever.commons.lua.LuaLoader;
import com.faforever.commons.map.PreviewGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveException;
import org.luaj.vm2.LuaValue;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.FileSystemUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.nocatch.NoCatch.noCatch;

@Service
@Slf4j
@AllArgsConstructor
public class MapService {
  private static final String[] REQUIRED_FILES = new String[]{
    ".scmap",
    "_save.lua",
    "_scenario.lua",
    "_script.lua"};
  private static final Charset MAP_CHARSET = StandardCharsets.ISO_8859_1;
  private static final String STUPID_MAP_FOLDER_PREFIX = "maps/";
  private static final int MAP_DISPLAY_NAME_MAX_LENGTH = 100;
  private final FafApiProperties fafApiProperties;
  private final MapRepository mapRepository;
  private final ContentService contentService;

  @Transactional
  @SneakyThrows
  @CacheEvict(value = {Map.TYPE_NAME, MapVersion.TYPE_NAME}, allEntries = true)
  public void uploadMap(byte[] mapData, String mapFilename, Player author, boolean isRanked) {
    Assert.notNull(author, "'author' must not be null");
    Assert.isTrue(mapData.length > 0, "'mapData' must not be empty");

    MapUploadData progressData = new MapUploadData()
      .setBaseDir(contentService.createTempDir())
      .setUploadFileName(mapFilename)
      .setAuthorEntity(author)
      .setRanked(isRanked);

    progressData.setUploadedFile(progressData.getBaseDir().resolve(mapFilename));
    copyToTemporaryDirectory(mapData, progressData);

    try {
      unzipFile(progressData);
      postProcessZipFiles(progressData);

      parseScenarioLua(progressData);
      checkLua(progressData);
      postProcessLuaFile(progressData);

      updateMapEntities(progressData);

      renameFolderNameAndCorrectPathInLuaFiles(progressData);
      generatePreview(progressData);

      zipMapData(progressData);
    } finally {
      cleanup(progressData);
    }
  }

  private Path copyToTemporaryDirectory(byte[] mapData, MapUploadData progressData) throws IOException {
    return Files.write(progressData.getUploadedFile(), mapData);
  }

  private void unzipFile(MapUploadData mapData) throws IOException, ArchiveException {
      Unzipper.from(mapData.getUploadedFile())
        .to(mapData.getBaseDir())
        .unzip();
  }

  private void postProcessZipFiles(MapUploadData mapUploadData) throws IOException {
    Optional<Path> mapFolder;
    try (Stream<Path> mapFolderStream = Files.list(mapUploadData.getBaseDir())) {
      mapFolder = mapFolderStream
        .filter(path -> Files.isDirectory(path))
        .findFirst();
    }

    if (!mapFolder.isPresent()) {
      throw new ApiException(new Error(ErrorCode.MAP_MISSING_MAP_FOLDER_INSIDE_ZIP));
    }

    try (Stream<Path> mapFolderStream = Files.list(mapUploadData.getBaseDir())) {
      if (mapFolderStream.count() != 2) {
        throw new ApiException(new Error(ErrorCode.MAP_INVALID_ZIP));
      }
    }

    mapUploadData.setOriginalMapFolder(mapFolder.get());
    mapUploadData.setUploadFolderName(mapUploadData.getOriginalMapFolder().getFileName().toString());

    List<Path> filePaths = new ArrayList<>();
    try (Stream<Path> mapFileStream = Files.list(mapUploadData.getOriginalMapFolder())) {
      mapFileStream.forEach(filePaths::add);
      Arrays.stream(REQUIRED_FILES)
        .forEach(filePattern -> {
          if (filePaths.stream()
            .noneMatch(filePath -> filePath.toString().endsWith(filePattern))) {
            throw new ApiException(new Error(ErrorCode.MAP_FILE_INSIDE_ZIP_MISSING, filePattern));
          }
        });
    }
  }

  private void parseScenarioLua(MapUploadData progressData) throws IOException {
    try (Stream<Path> mapFilesStream = Files.list(progressData.getOriginalMapFolder())) {
      Path scenarioLuaPath = noCatch(() -> mapFilesStream)
        .filter(myFile -> myFile.toString().endsWith("_scenario.lua"))
        .findFirst()
        .orElseThrow(() -> new ApiException(new Error(ErrorCode.MAP_SCENARIO_LUA_MISSING)));
      LuaValue root = noCatch(() -> LuaLoader.loadFile(scenarioLuaPath), IllegalStateException.class);
      progressData.setLuaRoot(root);
    }
  }

  private void checkLua(MapUploadData progressData) {
    List<Error> errors = new ArrayList<>();
    LuaValue scenarioInfo = progressData.getLuaScenarioInfo();
    if (scenarioInfo.get(ScenarioMapInfo.NAME) == LuaValue.NIL) {
      errors.add(new Error(ErrorCode.MAP_NAME_MISSING));
    }
    String displayName = scenarioInfo.get(ScenarioMapInfo.NAME).tojstring();
    if (displayName.length() > MAP_DISPLAY_NAME_MAX_LENGTH) {
      throw new ApiException(new Error(ErrorCode.MAP_NAME_TOO_LONG, MAP_DISPLAY_NAME_MAX_LENGTH, displayName.length()));
    }
    if (!NameUtil.isPrintableAsciiString(displayName)) {
      throw new ApiException(new Error(ErrorCode.MAP_NAME_INVALID));
    }

    if (scenarioInfo.get(ScenarioMapInfo.DESCRIPTION) == LuaValue.NIL) {
      errors.add(new Error(ErrorCode.MAP_DESCRIPTION_MISSING));
    }
    if (invalidTeam(scenarioInfo)) {
      errors.add(new Error(ErrorCode.MAP_FIRST_TEAM_FFA));
    }
    if (scenarioInfo.get(ScenarioMapInfo.TYPE) == LuaValue.NIL) {
      errors.add(new Error(ErrorCode.MAP_TYPE_MISSING));
    }
    if (scenarioInfo.get(ScenarioMapInfo.SIZE) == LuaValue.NIL) {
      errors.add(new Error(ErrorCode.MAP_SIZE_MISSING));
    }
    if (scenarioInfo.get(ScenarioMapInfo.MAP_VERSION) == LuaValue.NIL) {
      errors.add(new Error(ErrorCode.MAP_VERSION_MISSING));
    }
    if (!errors.isEmpty()) {
      throw new ApiException(errors.toArray(new Error[errors.size()]));
    }
  }

  private boolean invalidTeam(LuaValue scenarioInfo) {
    LuaValue scenario = scenarioInfo.get(ScenarioMapInfo.CONFIGURATIONS);
    if (scenario == LuaValue.NIL) {
      return true;
    }
    LuaValue standard = scenario.get(ScenarioMapInfo.CONFIGURATION_STANDARD);
    if (standard == LuaValue.NIL) {
      return true;
    }
    LuaValue teams = standard.get(ScenarioMapInfo.CONFIGURATION_STANDARD_TEAMS);
    if (teams == LuaValue.NIL) {
      return true;
    }
    LuaValue firstTeam = teams.get(1);
    if (firstTeam == LuaValue.NIL) {
      return true;
    }
    LuaValue teamName = firstTeam.get(ScenarioMapInfo.CONFIGURATION_STANDARD_TEAMS_NAME);
    if (teamName == LuaValue.NIL) {
      return true;
    }
    LuaValue armies = firstTeam.get(ScenarioMapInfo.CONFIGURATION_STANDARD_TEAMS_ARMIES);
    return armies == LuaValue.NIL || !teamName.tojstring().equals("FFA");

  }

  private void postProcessLuaFile(MapUploadData progressData) {
    LuaValue scenarioInfo = progressData.getLuaScenarioInfo();
    Optional<Map> mapEntity = mapRepository.findOneByDisplayName(
      scenarioInfo.get(ScenarioMapInfo.NAME).toString());
    if (!mapEntity.isPresent()) {
      return;
    }
    Player author = mapEntity.get().getAuthor();
    if (author == null) {
      return;
    }
    if (author.getId() != progressData.getAuthorEntity().getId()) {
      throw new ApiException(new Error(ErrorCode.MAP_NOT_ORIGINAL_AUTHOR, mapEntity.get().getDisplayName()));
    }
    int newVersion = scenarioInfo.get(ScenarioMapInfo.MAP_VERSION).toint();
    if (mapEntity.get().getVersions().stream()
      .anyMatch(mapVersion -> mapVersion.getVersion() == newVersion)) {
      throw new ApiException(new Error(ErrorCode.MAP_VERSION_EXISTS, mapEntity.get().getDisplayName(), newVersion));
    }
    progressData.setMapEntity(mapEntity.get());
  }

  private void updateMapEntities(MapUploadData progressData) {
    LuaValue scenarioInfo = progressData.getLuaScenarioInfo();
    Map map = progressData.getMapEntity();
    if (map == null) {
      map = new Map();
    }

    String name = scenarioInfo.get(ScenarioMapInfo.NAME).toString();

    map.setDisplayName(name)
      .setMapType(scenarioInfo.get(ScenarioMapInfo.TYPE).tojstring())
      .setBattleType(scenarioInfo.get(ScenarioMapInfo.CONFIGURATIONS).get(ScenarioMapInfo.CONFIGURATION_STANDARD).get(ScenarioMapInfo.CONFIGURATION_STANDARD_TEAMS).get(1)
        .get(ScenarioMapInfo.CONFIGURATION_STANDARD_TEAMS_NAME).tojstring())
      .setAuthor(progressData.getAuthorEntity());

    LuaValue size = scenarioInfo.get(ScenarioMapInfo.SIZE);
    MapVersion version = new MapVersion()
      .setDescription(scenarioInfo.get(ScenarioMapInfo.DESCRIPTION).tojstring().replaceAll("<LOC .*?>", ""))
      .setWidth(size.get(1).toint())
      .setHeight(size.get(2).toint())
      .setHidden(false)
      .setRanked(progressData.isRanked())
      .setMaxPlayers(scenarioInfo.get(ScenarioMapInfo.CONFIGURATIONS).get(ScenarioMapInfo.CONFIGURATION_STANDARD).get(ScenarioMapInfo.CONFIGURATION_STANDARD_TEAMS).get(1)
        .get(ScenarioMapInfo.CONFIGURATION_STANDARD_TEAMS_ARMIES).length())
      .setVersion(scenarioInfo.get(ScenarioMapInfo.MAP_VERSION).toint());
    map.getVersions().add(version);
    version.setMap(map);

    progressData.setMapEntity(map);
    progressData.setMapVersionEntity(version);

    version.setFilename(STUPID_MAP_FOLDER_PREFIX + progressData.getFinalZipName());
    progressData.setFinalZipFile(
      this.fafApiProperties.getMap().getTargetDirectory()
        .resolve(progressData.getFinalZipName()));

    if (Files.exists(progressData.getFinalZipFile())) {
      throw new ApiException(new Error(ErrorCode.MAP_NAME_CONFLICT, progressData.getFinalZipName()));
    }

    // this triggers validation
    mapRepository.save(map);
  }

  private void renameFolderNameAndCorrectPathInLuaFiles(MapUploadData progressData) throws IOException {
    progressData.setNewMapFolder(progressData.getBaseDir().resolve(progressData.getNewFolderName()));
    Files.move(progressData.getOriginalMapFolder(), progressData.getNewMapFolder());
    updateLuaFiles(progressData);
  }

  private void updateLuaFiles(MapUploadData mapData) throws IOException {
    String oldNameFolder = "/maps/" + mapData.getUploadFolderName();
    String newNameFolder = "/maps/" + mapData.getNewFolderName();
    try (Stream<Path> mapFileStream = Files.list(mapData.getNewMapFolder())) {
      mapFileStream
        .filter(path -> path.toString().toLowerCase().endsWith(".lua"))
        .forEach(path -> noCatch(() -> {
          List<String> lines = Files.readAllLines(path, MAP_CHARSET).stream()
            .map(line -> line.replaceAll("(?i)" + oldNameFolder, newNameFolder))
            .collect(Collectors.toList());
          Files.write(path, lines, MAP_CHARSET);
        }));
    }
  }

  private void generatePreview(MapUploadData mapData) throws IOException {
    String previewFilename = mapData.getNewFolderName() + ".png";
    generateImage(
      fafApiProperties.getMap().getDirectoryPreviewPathSmall().resolve(previewFilename),
      mapData.getNewMapFolder(),
      fafApiProperties.getMap().getPreviewSizeSmall());

    generateImage(
      fafApiProperties.getMap().getDirectoryPreviewPathLarge().resolve(previewFilename),
      mapData.getNewMapFolder(),
      fafApiProperties.getMap().getPreviewSizeLarge());
  }

  private void zipMapData(MapUploadData progressData) throws IOException, ArchiveException {
    cleanupBaseDir(progressData);
    Path finalZipFile = progressData.getFinalZipFile();
    Files.createDirectories(finalZipFile.getParent(), FilePermissionUtil.directoryPermissionFileAttributes());
    Zipper
      .contentOf(progressData.getBaseDir())
      .to(finalZipFile)
      .zip();
    // TODO if possible, this should be done using umask instead
    FilePermissionUtil.setDefaultFilePermission(finalZipFile);
  }

  private void cleanupBaseDir(MapUploadData progressData) throws IOException {
    Files.delete(progressData.getUploadedFile());
    try (Stream<Path> stream = Files.list(progressData.getBaseDir())) {
      if (stream.count() != 1) {
        throw new ProgrammingError("Folder containing unknown data: " + progressData.getBaseDir());
      }
    }
  }

  private void generateImage(Path target, Path baseDir, int size) throws IOException {
    BufferedImage image = PreviewGenerator.generatePreview(baseDir, size, size);
    if (target.getNameCount() > 0) {
      Files.createDirectories(target.getParent(), FilePermissionUtil.directoryPermissionFileAttributes());
    }
    ImageIO.write(image, "png", target.toFile());
  }

  private boolean cleanup(MapUploadData mapData) {
    return FileSystemUtils.deleteRecursively(mapData.getBaseDir().toFile());
  }

  @Data
  private class MapUploadData {
    private String uploadFileName;
    private String uploadFolderName;
    private String newFolderName;
    private Path uploadedFile;
    private Path baseDir;
    private Path originalMapFolder;
    private Path newMapFolder;
    private Path finalZipFile;
    private LuaValue luaRoot;
    private Map mapEntity;
    private MapVersion mapVersionEntity;
    private Player authorEntity;
    private boolean isRanked;
    private LuaValue scenarioInfo;

    private LuaValue getLuaScenarioInfo() {
      if (getLuaRoot() == null) {
        throw new IllegalStateException("*_scenario.lua parse result not available");
      }
      if (scenarioInfo == null) {
        scenarioInfo = getLuaRoot().get("ScenarioInfo");
      }
      return scenarioInfo;
    }

    private String getNewFolderName() {
      return generateNewMapNameWithVersion("");
    }

    private String generateNewMapNameWithVersion(String extension) {
      return Paths.get(String.format("%s.v%04d%s",
        NameUtil.normalizeFileName(mapEntity.getDisplayName()),
        mapVersionEntity.getVersion(),
        extension))
        .normalize().toString();
    }

    private String getFinalZipName() {
      return generateNewMapNameWithVersion(".zip");
    }
  }

  private class ScenarioMapInfo {
    private static final String CONFIGURATIONS = "Configurations";
    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";
    private static final String TYPE = "type";
    private static final String SIZE = "size";
    private static final String MAP_VERSION = "map_version";
    private static final String CONFIGURATION_STANDARD = "standard";
    private static final String CONFIGURATION_STANDARD_TEAMS = "teams";
    private static final String CONFIGURATION_STANDARD_TEAMS_NAME = "name";
    private static final String CONFIGURATION_STANDARD_TEAMS_ARMIES = "armies";
  }
}

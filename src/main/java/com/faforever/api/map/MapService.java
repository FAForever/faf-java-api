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
import com.faforever.api.utils.JavaFxUtil;
import com.faforever.commons.lua.LuaLoader;
import com.faforever.commons.map.PreviewGenerator;
import com.faforever.commons.zip.Unzipper;
import com.faforever.commons.zip.Zipper;
import javafx.scene.image.Image;
import lombok.Data;
import lombok.SneakyThrows;
import org.luaj.vm2.LuaValue;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;

import javax.inject.Inject;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static com.github.nocatch.NoCatch.noCatch;

@Service
public class MapService {
  private static final String[] REQUIRED_FILES = new String[]{
      ".scmap",
      "_save.lua",
      "_scenario.lua",
      "_script.lua"};
  private static final Charset MAP_CHARSET = StandardCharsets.ISO_8859_1;
  private final FafApiProperties fafApiProperties;
  private final MapRepository mapRepository;
  private final ContentService contentService;

  @Inject
  public MapService(FafApiProperties fafApiProperties, MapRepository mapRepository, ContentService contentService) {
    this.fafApiProperties = fafApiProperties;
    this.mapRepository = mapRepository;
    this.contentService = contentService;
  }

  @Transactional
  @SneakyThrows
  void uploadMap(byte[] mapData, String mapFilename, Player author, boolean isRanked) {
    if (author == null) {
      throw new ProgrammingError("'author' cannot be null");
    }
    if (mapData.length <= 0) {
      throw new IllegalArgumentException("'mapData' is empty");
    }
    MapUploadData progressData = new MapUploadData()
        .setBaseDir(contentService.createTempDir())
        .setUploadFileName(mapFilename)
        .setAuthorEntity(author)
        .setRanked(isRanked);

    progressData.setUploadedFile(progressData.getBaseDir().resolve(mapFilename));
    copyToTemporaryDirectory(mapData, progressData);

    unzipFile(progressData);
    postProcessZipFiles(progressData);

    parseScenarioLua(progressData);
    checkLua(progressData);
    postProcessLuaFile(progressData);

    updateMapEntities(progressData);

    renameFolderNameAndCorrectPathInLuaFiles(progressData);
    generatePreview(progressData);

    zipMapData(progressData);
    cleanup(progressData);
  }

  @SneakyThrows
  private Path copyToTemporaryDirectory(byte[] mapData, MapUploadData progressData) {
    return Files.write(progressData.getUploadedFile(), mapData);
  }

  @SneakyThrows
  private void unzipFile(MapUploadData mapData) {
    try (ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(
        Files.newInputStream(mapData.getUploadedFile())))) {
      Unzipper.from(zipInputStream).to(mapData.getBaseDir()).unzip();
    }
  }

  @SneakyThrows
  private void postProcessZipFiles(MapUploadData mapUploadData) {
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

  @SneakyThrows
  private void parseScenarioLua(MapUploadData progressData) {
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
    if (mapEntity.get().getAuthor().getId() != progressData.getAuthorEntity().getId()) {
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
    map.setDisplayName(scenarioInfo.get(ScenarioMapInfo.NAME).toString())
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

    version.setFilename(progressData.getFinalZipName());
    progressData.setFinalZipFile(
        this.fafApiProperties.getMap().getTargetDirectory()
            .resolve(progressData.getFinalZipName()));

    if (Files.exists(progressData.getFinalZipFile())) {
      throw new ApiException(new Error(ErrorCode.MAP_NAME_CONFLICT, progressData.getFinalZipName()));
    }

    // this triggers validation
    mapRepository.save(map);
  }

  @SneakyThrows
  private void renameFolderNameAndCorrectPathInLuaFiles(MapUploadData progressData) {
    progressData.setNewMapFolder(progressData.getBaseDir().resolve(progressData.getNewFolderName()));
    Files.move(progressData.getOriginalMapFolder(), progressData.getNewMapFolder());
    updateLuaFiles(progressData);
  }

  @SneakyThrows
  private void updateLuaFiles(MapUploadData mapData) {
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


  @SneakyThrows
  private void generatePreview(MapUploadData mapData) {
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

  @SneakyThrows
  private void zipMapData(MapUploadData progressData) {
    cleanupBaseDir(progressData);
    Files.createDirectories(progressData.getFinalZipFile().getParent());
    try (ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(
        Files.newOutputStream(progressData.getFinalZipFile())))) {
      Zipper.contentOf(progressData.getBaseDir()).to(zipOutputStream).zip();
    }
  }

  @SneakyThrows
  private void cleanupBaseDir(MapUploadData progressData) {
    Files.delete(progressData.getUploadedFile());
    try (Stream<Path> stream = Files.list(progressData.getBaseDir())) {
      if (stream.count() != 1) {
        throw new ProgrammingError("Folder containing unknown data: " + progressData.getBaseDir());
      }
    }
  }

  @SneakyThrows
  private void generateImage(Path target, Path baseDir, int size) {
    Image image = PreviewGenerator.generatePreview(baseDir, size, size);
    JavaFxUtil.writeImage(image, target, "png");
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

    private String normalizeMapName(String mapName) {
      return Paths.get(mapName.toLowerCase().replaceAll(" ", "_")).normalize().toString();
    }

    private String getNewFolderName() {
      return generateNewMapNameWithVersion("");
    }

    private String generateNewMapNameWithVersion(String extension) {
      return Paths.get(String.format("%s.v%04d%s",
          normalizeMapName(mapEntity.getDisplayName()),
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

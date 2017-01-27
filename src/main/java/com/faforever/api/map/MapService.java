package com.faforever.api.map;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.content.ContentService;
import com.faforever.api.data.domain.Map;
import com.faforever.api.data.domain.MapVersion;
import com.faforever.api.data.domain.Player;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.utils.JavaFxUtil;
import com.faforever.api.utils.PreviewGenerator;
import com.faforever.api.utils.Unzipper;
import com.faforever.api.utils.Zipper;
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
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static com.faforever.api.utils.LuaUtil.loadFile;
import static com.github.nocatch.NoCatch.noCatch;

@Service
public class MapService {
  private static final String[] REQUIRED_FILES = new String[]{
      ".scmap",
      "_save.lua",
      "_scenario.lua",
      "_script.lua"};
  public static final Charset MAP_CHARSET = StandardCharsets.ISO_8859_1;
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
  public void uploadMap(byte[] mapData, String mapFilename, Player author, boolean isRanked) throws IOException {
    if (author == null) {
      throw new IllegalStateException("author cannot be null");
    }
    if (mapData.length <= 0) {
      throw new IllegalStateException("mapData is empty");
    }
    MapUploadData progressData = new MapUploadData()
        .setBaseDir(contentService.createTempDir())
        .setUploadFileName(mapFilename)
        .setAuthorEntity(author)
        .setRanked(isRanked);

    progressData.setUploadedFile(Paths.get(progressData.getBaseDir().toString(), mapFilename));
    copyToTemporaryDirectory(mapData, progressData);

    unzipFile(progressData);
    postProcessZipFiles(progressData);

    parseScenarioLua(progressData);
    checkLua(progressData);
    postProcessLuaFile(progressData);

    updateMapEntities(progressData);

    renameFolderNameAndAdaptLuaFiles(progressData);
    generatePreview(progressData);

    zipMapData(progressData);
    cleanup(progressData);
  }

  private Path copyToTemporaryDirectory(byte[] mapData, MapUploadData progressData) throws IOException {
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
    mapUploadData.setOriginalMapFolder(mapFolder.get());
    mapUploadData.setUploadFolderName(mapUploadData.getOriginalMapFolder().getFileName().toString());

    List<Path> filePaths = new ArrayList<>();
    try (Stream<Path> mapFileStream = Files.list(mapUploadData.getOriginalMapFolder())) {
      mapFileStream.forEach(myPath -> filePaths.add(myPath));
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
      LuaValue root = noCatch(() -> loadFile(scenarioLuaPath), IllegalStateException.class);
      progressData.setLuaRoot(root);
    }
  }

  private void checkLua(MapUploadData progressData) {
    List<Error> errors = new ArrayList<>();
    LuaValue scenarioInfo = progressData.getLuaScenarioInfo();
    if (scenarioInfo.get("name") == LuaValue.NIL) {
      errors.add(new Error(ErrorCode.MAP_NAME_MISSING));
    }
    if (scenarioInfo.get("description") == LuaValue.NIL) {
      errors.add(new Error(ErrorCode.MAP_DESCRIPTION_MISSING));
    }
    if (invalidTeam(scenarioInfo)) {
      errors.add(new Error(ErrorCode.MAP_FIRST_TEAM_FFA));
    }
    if (scenarioInfo.get("type") == LuaValue.NIL) {
      errors.add(new Error(ErrorCode.MAP_TYPE_MISSING));
    }
    if (scenarioInfo.get("size") == LuaValue.NIL) {
      errors.add(new Error(ErrorCode.MAP_SIZE_MISSING));
    }
    if (scenarioInfo.get("map_version") == LuaValue.NIL) {
      errors.add(new Error(ErrorCode.MAP_VERSION_MISSING));
    }
    if(errors.size() > 0) {
      throw new ApiException(errors.toArray(new Error[0]));
    }
  }

  private boolean invalidTeam(LuaValue scenarioInfo) {
    return scenarioInfo.get("Configurations") == LuaValue.NIL
        || scenarioInfo.get("Configurations").get("standard") == LuaValue.NIL
        || scenarioInfo.get("Configurations").get("standard").get("teams") == LuaValue.NIL
        || scenarioInfo.get("Configurations").get("standard").get("teams").get(1) == LuaValue.NIL
        || scenarioInfo.get("Configurations").get("standard").get("teams").get(1).get("name") == LuaValue.NIL
        || scenarioInfo.get("Configurations").get("standard").get("teams").get(1).get("armies") == LuaValue.NIL
        || !scenarioInfo.get("Configurations").get("standard").get("teams").get(1).get("name").tojstring().equals("FFA");
  }

  private void postProcessLuaFile(MapUploadData progressData) {
    LuaValue scenarioInfo = progressData.getLuaScenarioInfo();
    Optional<Map> mapEntity = mapRepository.findOneByDisplayName(scenarioInfo.get("name").toString());
    if (mapEntity.isPresent()) {
      if (mapEntity.get().getAuthor().getId() != progressData.getAuthorEntity().getId()) {
        throw new ApiException(new Error(ErrorCode.MAP_NOT_ORIGINAL_AUTHOR, mapEntity.get().getDisplayName()));
      }
      int newVersion = scenarioInfo.get("map_version").toint();
      if (mapEntity.get().getVersions().stream()
          .anyMatch(mapVersion -> mapVersion.getVersion() == newVersion)) {
        throw new ApiException(new Error(ErrorCode.MAP_VERSION_EXISTS, mapEntity.get().getDisplayName(), newVersion));
      }
      progressData.setMapEntity(mapEntity.get());
    }
  }

  private void updateMapEntities(MapUploadData progressData) {
    LuaValue scenarioInfo = progressData.getLuaScenarioInfo();
    Map map = progressData.getMapEntity();
    if (map == null) {
      map = new Map();
    }
    map.setDisplayName(scenarioInfo.get("name").toString())
        .setMapType(scenarioInfo.get("type").tojstring())
        .setBattleType(scenarioInfo.get("Configurations").get("standard").get("teams").get(1).get("name").tojstring())
        .setAuthor(progressData.getAuthorEntity());

    LuaValue size = scenarioInfo.get("size");
    MapVersion version = new MapVersion()
        .setDescription(scenarioInfo.get("description").tojstring().replaceAll("<LOC .*?>", ""))
        .setWidth(size.get(1).toint())
        .setHeight(size.get(2).toint())
        .setHidden(false)
        .setRanked(progressData.isRanked())
        .setMaxPlayers(scenarioInfo.get("Configurations").get("standard").get("teams").get(1).get("armies").length())
        .setVersion(scenarioInfo.get("map_version").toint());
    map.getVersions().add(version);
    version.setMap(map);

    progressData.setMapEntity(map);
    progressData.setMapVersionEntity(version);

    version.setFilename(progressData.getFinalZipName());
    progressData.setFinalZipFile(Paths.get(
        this.fafApiProperties.getMap().getFinalDirectory(),
        progressData.getFinalZipName()));

    if (Files.exists(progressData.getFinalZipFile())) {
      throw new ApiException(new Error(ErrorCode.MAP_NAME_CONFLICT, progressData.getFinalZipName()));
    }

    // save entity to db to trigger validation
    mapRepository.save(map);
  }

  @SneakyThrows
  private void renameFolderNameAndAdaptLuaFiles(MapUploadData progressData) {
    progressData.setNewMapFolder(Paths.get(progressData.getBaseDir().toString(), progressData.getNewFolderName()));
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
            List<String> collect = Files.readAllLines(path, MAP_CHARSET).stream()
                .map(line -> line.replaceAll("(?i)" + oldNameFolder, newNameFolder))
                .collect(Collectors.toList());
            Files.write(path, collect, MAP_CHARSET);
          }));
    }
  }


  private void generatePreview(MapUploadData mapData) throws IOException {
    String previewFilename = mapData.getNewFolderName() + ".png";
    generateImage(Paths.get(
        fafApiProperties.getMap().getMapPreviewPathSmall(), previewFilename),
        mapData.getNewMapFolder(),
        fafApiProperties.getMap().getPreviewSizeSmall());

    generateImage(Paths.get(
        fafApiProperties.getMap().getMapPreviewPathLarge(), previewFilename),
        mapData.getNewMapFolder(),
        fafApiProperties.getMap().getPreviewSizeLarge());
  }

  @SneakyThrows
  private void zipMapData(MapUploadData progressData) {
    cleanupBaseDir(progressData);
    try (ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(
        Files.newOutputStream(progressData.getFinalZipFile())))) {
      Zipper.contentOf(progressData.getBaseDir()).to(zipOutputStream).zip();
    }
  }

  private void cleanupBaseDir(MapUploadData progressData) throws IOException {
    Files.delete(progressData.getUploadedFile());
    try (Stream<Path> stream = Files.list(progressData.getBaseDir())) {
      if (stream.count() != 1) {
        throw new IllegalStateException("Folder containing unknown data: " + progressData.getBaseDir());
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

    public LuaValue getLuaScenarioInfo() {
      if (getLuaRoot() == null) {
        throw new IllegalStateException("*_scenario.lua parse result not available");
      }
      return getLuaRoot().get("ScenarioInfo");
    }

    private String normalizeMapName(String mapName) {
      return Paths.get(mapName.toLowerCase().replaceAll(" ", "_")).normalize().toString();
    }

    public String getNewFolderName() {
      return generateNewMapNameWithVersion("");
    }

    public String generateNewMapNameWithVersion(String extension) {
      return Paths.get(String.format("%s.v%04d%s",
          normalizeMapName(mapEntity.getDisplayName()),
          mapVersionEntity.getVersion(),
          extension))
          .normalize().toString();
    }

    public String getFinalZipName() {
      return generateNewMapNameWithVersion(".zip");
    }
  }
}

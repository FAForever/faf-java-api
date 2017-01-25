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
import javafx.scene.image.Image;
import lombok.Data;
import lombok.SneakyThrows;
import org.luaj.vm2.LuaValue;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;

import javax.inject.Inject;
import java.io.BufferedInputStream;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipInputStream;

import static com.faforever.api.utils.LuaUtil.loadFile;
import static com.github.nocatch.NoCatch.noCatch;

@Service
public class MapService {
  private static final float MAP_SIZE_FACTOR = 51.2f;
  private static final String[] REQUIRED_FILES = new String[]{
      ".scmap",
      "_save.lua",
      "_scenario.lua",
      "_script.lua"};
  private static final String[] INVALID_MAP_NAME = new String[]{
      "save",
      "script",
      "map",
      "tables"
  };
  public static final Charset MAP_CHARSET = StandardCharsets.ISO_8859_1;
  private final FafApiProperties fafApiProperties;
  private final MapRepository mapRepository;
  private final ContentService contentService;
  private final Pattern luaMapPattern;

  @Inject
  public MapService(FafApiProperties fafApiProperties, MapRepository mapRepository, ContentService contentService) {
    this.fafApiProperties = fafApiProperties;
    this.mapRepository = mapRepository;
    this.contentService = contentService;
    this.luaMapPattern = Pattern.compile("([^/]+)\\.scmap");
  }

  @Transactional
  public void uploadMap(byte[] mapData, String mapFilename, Player author, boolean isRanked) throws IOException {
    // TODO: validate input: mapFilename, mapFilename, author
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
    postProcessLuaFile(progressData);

    updateMapEntities(progressData);

    renameFolderNameAndAdaptLuaFiles(progressData);
    generatePreview(progressData);

    zipMapData(progressData);
    copyToFinalDestination(progressData);
    cleanup(progressData);
  }

  @SneakyThrows
  private void copyToFinalDestination(MapUploadData progressData) {
//    Files.createDirectories(progressData.getFinalFile().getParent());
    // TODO: normalize zip file and repack it https://github.com/FAForever/faftools/blob/87f0275b889e5dd1b1545252a220186732e77403/faf/tools/fa/maps.py#L222
//    Files.createDirectories(finalPath.getParent());
//    enrichMapDataAndZip(mapFolder.get(), oldMapName, version);
  }

  private void zipMapData(MapUploadData progressData) {
    // FIXME
  }

  private void renameFolderNameAndAdaptLuaFiles(MapUploadData progressData) {
    // FIXME
    progressData.setNewMapFolder(progressData.getOriginalMapFolder());
  }

  private Path copyToTemporaryDirectory(byte[] mapData, MapUploadData progressData) throws IOException {
    return Files.write(progressData.getUploadedFile(), mapData);
  }

  private boolean cleanup(MapUploadData mapData) {
    return FileSystemUtils.deleteRecursively(mapData.getBaseDir().toFile());
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
        .setWidth((int) (size.get(1).toint() / MAP_SIZE_FACTOR))
        .setHeight((int) (size.get(2).toint() / MAP_SIZE_FACTOR))
        .setHidden(false)
        .setRanked(progressData.isRanked())
        .setMaxPlayers(scenarioInfo.get("Configurations").get("standard").get("teams").get(1).get("armies").length())
        .setVersion(scenarioInfo.get("map_version").toint());
    version.setFilename(generateMapName(map, version, "zip"));
    map.getVersions().add(version);
    version.setMap(map);

    // save entity to db to trigger validation
    // TODO: Manual test if transaction is reverted on exception
    mapRepository.save(map);
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

  @SneakyThrows
  private void parseScenarioLua(MapUploadData progressData) {
    Path scenarioLuaPath;
    // read from Lua File
    try (Stream<Path> mapFilesStream = Files.list(progressData.getOriginalMapFolder())) {
      scenarioLuaPath = noCatch(() -> mapFilesStream)
          .filter(myFile -> myFile.toString().endsWith("_scenario.lua"))
          .findFirst()
          .orElseThrow(() -> new ApiException(new Error(ErrorCode.MAP_SCENARIO_LUA_MISSING)));
    }

    LuaValue root = noCatch(() -> loadFile(scenarioLuaPath), IllegalStateException.class);
    progressData.setLuaRoot(root);
  }


  @SneakyThrows
  private void unzipFile(MapUploadData mapData) {
    try (ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(Files.newInputStream(mapData.getUploadedFile())))) {
      Unzipper.from(zipInputStream).to(mapData.getBaseDir()).unzip();
    }
  }

  private String generateMapName(Map map, MapVersion version, String extension) {
    return Paths.get(String.format("%s.v%04d.%s",
        normalizeMapName(map.getDisplayName()),
        version.getVersion(),
        extension))
        .normalize().toString();
  }

  private String normalizeMapName(String mapName) {
    return Paths.get(mapName.toLowerCase().replaceAll(" ", "_")).normalize().toString();
  }

  @SneakyThrows
  private void enrichMapDataAndZip(Path mapFolder, String oldMapName, MapVersion mapVersion) {
    Path finalPath = Paths.get(fafApiProperties.getMap().getFinalDirectory(), mapVersion.getFilename());
    if (Files.exists(finalPath)) {
      throw new ApiException(new Error(ErrorCode.MAP_NAME_CONFLICT, mapVersion.getFilename()));
    }
    String newMapName = com.google.common.io.Files.getNameWithoutExtension(mapVersion.getFilename());
    Path newMapFolder = Paths.get(mapFolder.getParent().toString(), newMapName);
    if (!newMapName.equals(oldMapName)) {
      renameFiles(mapFolder, oldMapName, newMapName);
      updateLua(mapFolder, oldMapName, mapVersion.getMap());
      Files.move(mapFolder, newMapFolder);
    }

//    try (ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(finalPath)))) {
//      Zipper.contentOf(mapFolder).to(zipOutputStream).zip();
//    }
  }

  @SneakyThrows
  private void updateLua(Path mapFolder, String oldMapName, Map map) {
    String oldNameFolder = "/maps/" + oldMapName;
    String newNameFolder = "/maps/" + normalizeMapName(map.getDisplayName());
    String oldName = "/" + oldMapName;
    String newName = "/" + normalizeMapName(map.getDisplayName());
    try (Stream<Path> mapFileStream = Files.list(mapFolder)) {
      mapFileStream.forEach(path -> noCatch(() -> {
        List<String> collect = Files.readAllLines(path, MAP_CHARSET).stream()
            .map(line -> line.replaceAll(oldNameFolder, newNameFolder)
                .replaceAll(oldName, newName))
            .collect(Collectors.toList());
        Files.write(path, collect, MAP_CHARSET);
      }));
    }
  }

  @SneakyThrows
  private void renameFiles(Path mapFolder, String oldMapName, String newMapName) {
    try (Stream<Path> mapFileStream = Files.list(mapFolder)) {
      mapFileStream.forEach(path -> {
        String filename = com.google.common.io.Files.getNameWithoutExtension(path.toString());
        if (filename.equalsIgnoreCase(oldMapName)) {
          try {
            Files.move(path, Paths.get(path.getParent().toString(), newMapName));
          } catch (IOException e) {
            throw new ApiException(new Error(ErrorCode.MAP_RENAME_FAILED));
          }
        }
      });
    }
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

  private void generateImage(Path target, Path baseDir, int size) throws IOException {
    Image image = PreviewGenerator.generatePreview(baseDir, size, size);
    JavaFxUtil.writeImage(image, target, "png");
  }

  // TODO: use this
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
  }
}

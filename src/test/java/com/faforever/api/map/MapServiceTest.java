package com.faforever.api.map;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.config.FafApiProperties.Map;
import com.faforever.api.content.ContentService;
import com.faforever.api.data.domain.MapVersion;
import com.faforever.api.data.domain.Player;
import com.faforever.api.data.domain.PlayerAchievement;
import com.faforever.api.error.ApiExceptionWithMutlipleCodes;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.utils.Unzipper;
import com.google.common.io.ByteStreams;
import junitx.framework.FileAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.util.FileSystemUtils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.ZipInputStream;

import static com.faforever.api.error.ApiExceptionWithCode.apiExceptionWithCode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// TODO: test_maps_upload_is_metadata_missing
// TODO: test_maps_upload_no_file_results_400
// TODO: test_maps_upload_txt_results_400
@RunWith(MockitoJUnitRunner.class)
public class MapServiceTest {
  private MapService instance;
  private Map mapProperties;

  @Rule
  public TemporaryFolder temporaryDirectory = new TemporaryFolder();
  @Rule
  public TemporaryFolder finalDirectory = new TemporaryFolder();
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private MapRepository mapRepository;
  @Mock
  private FafApiProperties fafApiProperties;
  @Mock
  private ContentService contentService;
  @Mock
  private Player author;

  @Before
  public void setUp() {
    instance = new MapService(fafApiProperties, mapRepository, contentService);
    mapProperties = new Map()
        .setFinalDirectory(finalDirectory.getRoot().getAbsolutePath())
        .setMapPreviewPathLarge(Paths.get(finalDirectory.getRoot().getAbsolutePath(), "large").toString())
        .setMapPreviewPathSmall(Paths.get(finalDirectory.getRoot().getAbsolutePath(), "small").toString());
    when(fafApiProperties.getMap()).thenReturn(mapProperties);
    when(contentService.createTempDir()).thenReturn(temporaryDirectory.getRoot().toPath());
  }

  @After
  public void shutDown() {
    if (Files.exists(temporaryDirectory.getRoot().toPath())) {
      FileSystemUtils.deleteRecursively(temporaryDirectory.getRoot());
    }
    if (Files.exists(temporaryDirectory.getRoot().toPath())) {
      FileSystemUtils.deleteRecursively(finalDirectory.getRoot());
    }
  }

  @Test
  public void zipFilenameAllreadyExists() throws IOException {
    Path clashedMap = Paths.get(finalDirectory.getRoot().getAbsolutePath(), "sludge_test.v0001.zip");
    clashedMap.toFile().createNewFile();
    String zipFilename = "scmp_037.zip";
    when(mapRepository.findOneByDisplayName(any())).thenReturn(Optional.empty());
    try (InputStream inputStream = loadMapResourceAsStream(zipFilename)) {
      byte[] mapData = ByteStreams.toByteArray(inputStream);
      expectedException.expect(apiExceptionWithCode(ErrorCode.MAP_NAME_CONFLICT));
      instance.uploadMap(mapData, zipFilename, author, true);
      verify(mapRepository, Mockito.never()).save(any(com.faforever.api.data.domain.Map.class));
    }
  }

  @Test
  public void emptyZip() throws IOException {
    String zipFilename = "empty.zip";
    try (InputStream inputStream = loadMapResourceAsStream(zipFilename)) {
      byte[] mapData = ByteStreams.toByteArray(inputStream);
      expectedException.expect(apiExceptionWithCode(ErrorCode.MAP_MISSING_MAP_FOLDER_INSIDE_ZIP));
      instance.uploadMap(mapData, zipFilename, author, true);
      verify(mapRepository, Mockito.never()).save(any(com.faforever.api.data.domain.Map.class));
    }
  }

  @Test
  public void notCorrectAuthor() throws IOException {
    String zipFilename = "scmp_037.zip";

    Player me = new Player();
    me.setId(1);
    Player bob = new Player();
    bob.setId(2);

    com.faforever.api.data.domain.Map map = new com.faforever.api.data.domain.Map().setAuthor(bob);
    when(mapRepository.findOneByDisplayName(any())).thenReturn(Optional.of(map));
    try (InputStream inputStream = loadMapResourceAsStream(zipFilename)) {
      byte[] mapData = ByteStreams.toByteArray(inputStream);
      expectedException.expect(apiExceptionWithCode(ErrorCode.MAP_NOT_ORIGINAL_AUTHOR));
      instance.uploadMap(mapData, zipFilename, me, true);
      verify(mapRepository, Mockito.never()).save(any(com.faforever.api.data.domain.Map.class));
    }
  }

  @Test
  public void versionExistsAlready() throws IOException {
    String zipFilename = "scmp_037.zip";

    Player me = new Player();
    me.setId(1);

    com.faforever.api.data.domain.Map map = new com.faforever.api.data.domain.Map()
        .setAuthor(me)
        .setVersions(Arrays.asList(new MapVersion().setVersion(1)));

    when(mapRepository.findOneByDisplayName(any())).thenReturn(Optional.of(map));
    try (InputStream inputStream = loadMapResourceAsStream(zipFilename)) {
      byte[] mapData = ByteStreams.toByteArray(inputStream);
      expectedException.expect(apiExceptionWithCode(ErrorCode.MAP_VERSION_EXISTS));
      instance.uploadMap(mapData, zipFilename, me, true);
      verify(mapRepository, Mockito.never()).save(any(com.faforever.api.data.domain.Map.class));
    }
  }

  @Test
  public void fileIsMissingInsideZip() throws IOException {
    String[] files = new String[]{"without_scmap.zip", "without_scenariolua.zip", "without_scmap.zip", "without_scriptlua.zip"};
    for (String zipFilename : files) {
      try (InputStream inputStream = loadMapResourceAsStream(zipFilename)) {
        byte[] mapData = ByteStreams.toByteArray(inputStream);
        expectedException.expect(apiExceptionWithCode(ErrorCode.MAP_FILE_INSIDE_ZIP_MISSING));
        instance.uploadMap(mapData, zipFilename, author, true);
        verify(mapRepository, Mockito.never()).save(any(com.faforever.api.data.domain.Map.class));
      }
    }
  }

  @Test
  public void battleTypeNotFFA() throws IOException {
    String zipFilename = "wrong_team_name.zip";
    when(mapRepository.findOneByDisplayName(any())).thenReturn(Optional.empty());
    try (InputStream inputStream = loadMapResourceAsStream(zipFilename)) {
      byte[] mapData = ByteStreams.toByteArray(inputStream);
      expectedException.expect(apiExceptionWithCode(ErrorCode.MAP_FIRST_TEAM_FFA));
      instance.uploadMap(mapData, zipFilename, author, true);
    }
  }

  @Test
  public void invalidScenario() throws IOException {
    String zipFilename = "invalid_scenario.zip";
    when(mapRepository.findOneByDisplayName(any())).thenReturn(Optional.empty());
    try (InputStream inputStream = loadMapResourceAsStream(zipFilename)) {
      byte[] mapData = ByteStreams.toByteArray(inputStream);
      expectedException.expect(ApiExceptionWithMutlipleCodes.apiExceptionWithCode(
          ErrorCode.MAP_NAME_MISSING,
          ErrorCode.MAP_DESCRIPTION_MISSING,
          ErrorCode.MAP_FIRST_TEAM_FFA,
          ErrorCode.MAP_TYPE_MISSING,
          ErrorCode.MAP_SIZE_MISSING,
          ErrorCode.MAP_VERSION_MISSING));
      instance.uploadMap(mapData, zipFilename, author, true);
    }
  }

  @Test
  public void positiveUploadTest() throws IOException {
    String zipFilename = "scmp_037.zip";
    when(mapRepository.findOneByDisplayName(any())).thenReturn(Optional.empty());
    try (InputStream inputStream = loadMapResourceAsStream(zipFilename)) {
      byte[] mapData = ByteStreams.toByteArray(inputStream);

      Path tmpDir = temporaryDirectory.getRoot().toPath();
      instance.uploadMap(mapData, zipFilename, author, true);

      ArgumentCaptor<com.faforever.api.data.domain.Map> mapCaptor = ArgumentCaptor.forClass(com.faforever.api.data.domain.Map.class);
      verify(mapRepository, Mockito.times(1)).save(mapCaptor.capture());
      assertEquals("Sludge_Test", mapCaptor.getValue().getDisplayName());
      assertEquals("skirmish", mapCaptor.getValue().getMapType());
      assertEquals("FFA", mapCaptor.getValue().getBattleType());
      assertEquals(1, mapCaptor.getValue().getVersions().size());

      MapVersion mapVersion = mapCaptor.getValue().getVersions().get(0);
      assertEquals("The thick, brackish water clings to everything, staining anything it touches. If it weren't for this planet's proximity to the Quarantine Zone, no one would ever bother coming here.", mapVersion.getDescription());
      assertEquals(1, mapVersion.getVersion());
      assertEquals(256, mapVersion.getHeight());
      assertEquals(256, mapVersion.getWidth());
      assertEquals(3, mapVersion.getMaxPlayers());
      assertEquals("sludge_test.v0001.zip", mapVersion.getFilename());

      assertFalse(Files.exists(tmpDir));

      Path generatedFile = Paths.get(finalDirectory.getRoot().getAbsolutePath(), "sludge_test.v0001.zip");
      assertTrue(Files.exists(generatedFile));

      Path generatedFiles = Paths.get(finalDirectory.getRoot().getAbsolutePath(), "generated_files");
      try (ZipInputStream inputStreamOfExpectedFile = new ZipInputStream(
          new BufferedInputStream(new FileInputStream(generatedFile.toFile())))) {
        Unzipper.from(inputStreamOfExpectedFile).to(generatedFiles).unzip();
      }

      Path expectedFiles = Paths.get(finalDirectory.getRoot().getAbsolutePath(), "expected_files");
      try (ZipInputStream inputStreamOfExpectedFile = new ZipInputStream(new BufferedInputStream(
          loadMapResourceAsStream("sludge_test.v0001.zip")))) {
        Unzipper.from(inputStreamOfExpectedFile).to(expectedFiles).unzip();
      }

      expectedFiles = expectedFiles.resolve("sludge_test.v0001");
      try (Stream<Path> fileStream = Files.list(expectedFiles)) {
        assertEquals(fileStream.count(), (long) 4);
      }

      try (Stream<Path> fileStream = Files.list(expectedFiles)) {
        Path finalGeneratedFile = generatedFiles.resolve("sludge_test.v0001");
        fileStream.forEach(expectedFile ->
            FileAssert.assertEquals("Difference in " + expectedFile.getFileName().toString(),
                expectedFile.toFile(),
                finalGeneratedFile.resolve(expectedFile.getFileName().toString()).toFile())

        );

        assertTrue(Files.exists(Paths.get(mapProperties.getMapPreviewPathLarge(), "sludge_test.v0001.png")));
        assertTrue(Files.exists(Paths.get(mapProperties.getMapPreviewPathSmall(), "sludge_test.v0001.png")));
      }
    }
  }


  private InputStream loadMapResourceAsStream(String filename) {
    return MapServiceTest.class.getResourceAsStream("/maps/" + filename);
  }
}

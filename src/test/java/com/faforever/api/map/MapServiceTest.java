package com.faforever.api.map;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.config.FafApiProperties.Map;
import com.faforever.api.content.ContentService;
import com.faforever.api.data.domain.MapVersion;
import com.faforever.api.data.domain.Player;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.ApiExceptionWithMultipleCodes;
import com.faforever.api.error.ErrorCode;
import com.faforever.commons.io.Unzipper;
import com.google.common.io.ByteStreams;
import com.googlecode.zohhak.api.TestWith;
import com.googlecode.zohhak.api.runners.ZohhakRunner;
import junitx.framework.FileAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.util.FileSystemUtils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.ZipInputStream;

import static com.faforever.api.error.ApiExceptionWithCode.apiExceptionWithCode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(ZohhakRunner.class)
public class MapServiceTest {
  @Rule
  public final TemporaryFolder temporaryDirectory = new TemporaryFolder();
  @Rule
  public final TemporaryFolder finalDirectory = new TemporaryFolder();
  @Rule
  public final ExpectedException expectedException = ExpectedException.none();
  private final MapRepository mapRepository = mock(MapRepository.class);
  private final FafApiProperties fafApiProperties = mock(FafApiProperties.class);
  private final ContentService contentService = mock(ContentService.class);
  private final Player author = mock(Player.class);
  private MapService instance;
  private Map mapProperties;

  @Before
  public void setUp() {
    instance = new MapService(fafApiProperties, mapRepository, contentService);
    mapProperties = new Map()
      .setTargetDirectory(finalDirectory.getRoot().toPath())
      .setDirectoryPreviewPathLarge(finalDirectory.getRoot().toPath().resolve("large"))
      .setDirectoryPreviewPathSmall(finalDirectory.getRoot().toPath().resolve("small"));
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
  public void zipFilenamealreadyExists() throws IOException {
    Path clashedMap = finalDirectory.getRoot().toPath().resolve("sludge_test.v0001.zip");
    assertTrue(clashedMap.toFile().createNewFile());
    String zipFilename = "scmp_037.zip";
    when(mapRepository.findOneByDisplayName(any())).thenReturn(Optional.empty());
    try (InputStream inputStream = loadMapResourceAsStream(zipFilename)) {
      try {
        byte[] mapData = ByteStreams.toByteArray(inputStream);
        instance.uploadMap(mapData, zipFilename, author, true);
        fail();
      } catch (ApiException e) {
        assertThat(e, apiExceptionWithCode(ErrorCode.MAP_NAME_CONFLICT));
      }
      verify(mapRepository, never()).save(any(com.faforever.api.data.domain.Map.class));
    }
  }

  @Test
  public void emptyZip() throws IOException {
    String zipFilename = "empty.zip";
    try (InputStream inputStream = loadMapResourceAsStream(zipFilename)) {
      try {
        byte[] mapData = ByteStreams.toByteArray(inputStream);
        instance.uploadMap(mapData, zipFilename, author, true);
        fail();
      } catch (ApiException e) {
        assertThat(e, apiExceptionWithCode(ErrorCode.MAP_MISSING_MAP_FOLDER_INSIDE_ZIP));
      }
      verify(mapRepository, never()).save(any(com.faforever.api.data.domain.Map.class));
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
      try {
        byte[] mapData = ByteStreams.toByteArray(inputStream);
        instance.uploadMap(mapData, zipFilename, me, true);
        fail();
      } catch (ApiException e) {
        assertThat(e, apiExceptionWithCode(ErrorCode.MAP_NOT_ORIGINAL_AUTHOR));
      }
      verify(mapRepository, never()).save(any(com.faforever.api.data.domain.Map.class));
    }
  }

  @Test
  public void versionExistsAlready() throws IOException {
    String zipFilename = "scmp_037.zip";

    Player me = new Player();
    me.setId(1);

    com.faforever.api.data.domain.Map map = new com.faforever.api.data.domain.Map()
      .setAuthor(me)
      .setVersions(Collections.singletonList(new MapVersion().setVersion(1)));

    when(mapRepository.findOneByDisplayName(any())).thenReturn(Optional.of(map));
    try (InputStream inputStream = loadMapResourceAsStream(zipFilename)) {
      try {
        byte[] mapData = ByteStreams.toByteArray(inputStream);
        instance.uploadMap(mapData, zipFilename, me, true);
        fail();
      } catch (ApiException e) {
        assertThat(e, apiExceptionWithCode(ErrorCode.MAP_VERSION_EXISTS));
      }
      verify(mapRepository, never()).save(any(com.faforever.api.data.domain.Map.class));
    }
  }

  @TestWith({"without_savelua.zip", "without_scenariolua.zip", "without_scmap.zip", "without_scriptlua.zip"})
  public void fileIsMissingInsideZip(String zipFilename) throws IOException {
    try (InputStream inputStream = loadMapResourceAsStream(zipFilename)) {
      try {
        byte[] mapData = ByteStreams.toByteArray(inputStream);
        instance.uploadMap(mapData, zipFilename, author, true);
        fail();
      } catch (ApiException e) {
        assertThat(e, apiExceptionWithCode(ErrorCode.MAP_FILE_INSIDE_ZIP_MISSING));
      }
      verify(mapRepository, never()).save(any(com.faforever.api.data.domain.Map.class));
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
    verify(mapRepository, never()).save(any(com.faforever.api.data.domain.Map.class));
  }

  @Test
  public void invalidScenario() throws IOException {
    String zipFilename = "invalid_scenario.zip";
    when(mapRepository.findOneByDisplayName(any())).thenReturn(Optional.empty());
    try (InputStream inputStream = loadMapResourceAsStream(zipFilename)) {
      byte[] mapData = ByteStreams.toByteArray(inputStream);
      expectedException.expect(ApiExceptionWithMultipleCodes.apiExceptionWithCode(
        ErrorCode.MAP_NAME_MISSING,
        ErrorCode.MAP_DESCRIPTION_MISSING,
        ErrorCode.MAP_FIRST_TEAM_FFA,
        ErrorCode.MAP_TYPE_MISSING,
        ErrorCode.MAP_SIZE_MISSING,
        ErrorCode.MAP_VERSION_MISSING));
      instance.uploadMap(mapData, zipFilename, author, true);
    }
    verify(mapRepository, never()).save(any(com.faforever.api.data.domain.Map.class));
  }

  @Test
  public void mapWithMoreRootFoldersInZip() throws IOException {
    String zipFilename = "scmp_037_invalid.zip";
    when(mapRepository.findOneByDisplayName(any())).thenReturn(Optional.empty());
    try (InputStream inputStream = loadMapResourceAsStream(zipFilename)) {
      byte[] mapData = ByteStreams.toByteArray(inputStream);
      try {
        instance.uploadMap(mapData, zipFilename, author, true);
      } catch (ApiException e) {
        assertThat(e, apiExceptionWithCode(ErrorCode.MAP_INVALID_ZIP));
      }
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
      assertEquals("maps/sludge_test.v0001.zip", mapVersion.getFilename());

      assertFalse(Files.exists(tmpDir));

      Path generatedFile = finalDirectory.getRoot().toPath().resolve("sludge_test.v0001.zip");
      assertTrue(Files.exists(generatedFile));

      Path generatedFiles = finalDirectory.getRoot().toPath().resolve("generated_files");
      try (ZipInputStream inputStreamOfExpectedFile = new ZipInputStream(
        new BufferedInputStream(new FileInputStream(generatedFile.toFile())))) {
        Unzipper.from(inputStreamOfExpectedFile).to(generatedFiles).unzip();
      }

      Path expectedFiles = finalDirectory.getRoot().toPath().resolve("expected_files");
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

        assertTrue(Files.exists(mapProperties.getDirectoryPreviewPathLarge().resolve("sludge_test.v0001.png")));
        assertTrue(Files.exists(mapProperties.getDirectoryPreviewPathSmall().resolve("sludge_test.v0001.png")));
      }
    }
  }

  private InputStream loadMapResourceAsStream(String filename) {
    return MapServiceTest.class.getResourceAsStream("/maps/" + filename);
  }
}

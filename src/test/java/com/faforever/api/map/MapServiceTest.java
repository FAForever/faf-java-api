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
import junitx.framework.FileAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import static com.faforever.api.error.ApiExceptionWithCode.apiExceptionWithCode;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MapServiceTest {
  @TempDir
  Path baseTemporaryDirectory;

  private Path temporaryDirectory;
  private Path finalDirectory;

  @Mock
  private MapRepository mapRepository;
  @Mock
  private FafApiProperties fafApiProperties;
  @Mock
  private ContentService contentService;
  @Mock
  private Player author;

  private MapService instance;
  private Map mapProperties;

  @BeforeEach
  void setUp() throws Exception {
    temporaryDirectory = Files.createDirectory(baseTemporaryDirectory.resolve("temp"));
    finalDirectory = Files.createDirectory(baseTemporaryDirectory.resolve("final"));

    instance = new MapService(fafApiProperties, mapRepository, contentService);
    mapProperties = new Map()
      .setTargetDirectory(finalDirectory)
      .setDirectoryPreviewPathLarge(finalDirectory.resolve("large"))
      .setDirectoryPreviewPathSmall(finalDirectory.resolve("small"));
    when(contentService.createTempDir()).thenReturn(temporaryDirectory);
  }

  @AfterEach
  void shutDown() throws Exception {
    if (Files.exists(temporaryDirectory)) {
      FileSystemUtils.deleteRecursively(temporaryDirectory);
    }
    if (Files.exists(temporaryDirectory)) {
      FileSystemUtils.deleteRecursively(finalDirectory);
    }
  }

  @Test
  void zipFilenameAlreadyExists() throws IOException {
    Path clashedMap = finalDirectory.resolve("sludge_test____.___..v0001.zip");
    assertTrue(clashedMap.toFile().createNewFile());
    String zipFilename = "scmp_037.zip";
    when(fafApiProperties.getMap()).thenReturn(mapProperties);
    when(mapRepository.findOneByDisplayName(any())).thenReturn(Optional.empty());
    try (InputStream inputStream = loadMapResourceAsStream(zipFilename)) {
      byte[] mapData = ByteStreams.toByteArray(inputStream);
      ApiException result = assertThrows(ApiException.class, () -> instance.uploadMap(mapData, zipFilename, author, true));
      assertThat(result, apiExceptionWithCode(ErrorCode.MAP_NAME_CONFLICT));
      verify(mapRepository, never()).save(any(com.faforever.api.data.domain.Map.class));
    }
  }

  @Test
  void emptyZip() throws IOException {
    String zipFilename = "empty.zip";
    try (InputStream inputStream = loadMapResourceAsStream(zipFilename)) {
      byte[] mapData = ByteStreams.toByteArray(inputStream);
      ApiException result = assertThrows(ApiException.class, () -> instance.uploadMap(mapData, zipFilename, author, true));
      assertThat(result, apiExceptionWithCode(ErrorCode.MAP_MISSING_MAP_FOLDER_INSIDE_ZIP));
      verify(mapRepository, never()).save(any(com.faforever.api.data.domain.Map.class));
    }
  }

  @Test
  void notCorrectAuthor() throws IOException {
    String zipFilename = "scmp_037.zip";

    Player me = new Player();
    me.setId(1);
    Player bob = new Player();
    bob.setId(2);

    com.faforever.api.data.domain.Map map = new com.faforever.api.data.domain.Map().setAuthor(bob);
    when(mapRepository.findOneByDisplayName(any())).thenReturn(Optional.of(map));
    try (InputStream inputStream = loadMapResourceAsStream(zipFilename)) {
      byte[] mapData = ByteStreams.toByteArray(inputStream);
      ApiException result = assertThrows(ApiException.class, () -> instance.uploadMap(mapData, zipFilename, me, true));
      assertThat(result, apiExceptionWithCode(ErrorCode.MAP_NOT_ORIGINAL_AUTHOR));
      verify(mapRepository, never()).save(any(com.faforever.api.data.domain.Map.class));
    }
  }

  @Test
  void versionExistsAlready() throws IOException {
    String zipFilename = "scmp_037.zip";

    Player me = new Player();
    me.setId(1);

    com.faforever.api.data.domain.Map map = new com.faforever.api.data.domain.Map()
      .setAuthor(me)
      .setVersions(Collections.singletonList(new MapVersion().setVersion(1)));

    when(mapRepository.findOneByDisplayName(any())).thenReturn(Optional.of(map));
    try (InputStream inputStream = loadMapResourceAsStream(zipFilename)) {
      byte[] mapData = ByteStreams.toByteArray(inputStream);
      ApiException result = assertThrows(ApiException.class, () -> instance.uploadMap(mapData, zipFilename, me, true));
      assertThat(result, apiExceptionWithCode(ErrorCode.MAP_VERSION_EXISTS));
      verify(mapRepository, never()).save(any(com.faforever.api.data.domain.Map.class));
    }

  }

  @ParameterizedTest
  @ValueSource(strings = {"without_savelua.zip", "without_scenariolua.zip", "without_scmap.zip", "without_scriptlua.zip"})
  void fileIsMissingInsideZip(String zipFilename) throws IOException {
    try (InputStream inputStream = loadMapResourceAsStream(zipFilename)) {
      byte[] mapData = ByteStreams.toByteArray(inputStream);
      ApiException result = assertThrows(ApiException.class, () -> instance.uploadMap(mapData, zipFilename, author, true));
      assertThat(result, apiExceptionWithCode(ErrorCode.MAP_FILE_INSIDE_ZIP_MISSING));
      verify(mapRepository, never()).save(any(com.faforever.api.data.domain.Map.class));
    }
  }

  @Test
  void battleTypeNotFFA() throws IOException {
    String zipFilename = "wrong_team_name.zip";
    try (InputStream inputStream = loadMapResourceAsStream(zipFilename)) {
      byte[] mapData = ByteStreams.toByteArray(inputStream);

      ApiException result = assertThrows(ApiException.class, () -> instance.uploadMap(mapData, zipFilename, author, true));
      assertThat(result, is(apiExceptionWithCode(ErrorCode.MAP_FIRST_TEAM_FFA)));
    }
    verify(mapRepository, never()).save(any(com.faforever.api.data.domain.Map.class));
  }

  @Test
  void invalidScenario() throws IOException {
    String zipFilename = "invalid_scenario.zip";
    try (InputStream inputStream = loadMapResourceAsStream(zipFilename)) {
      byte[] mapData = ByteStreams.toByteArray(inputStream);
      ApiException result = assertThrows(ApiException.class, () -> instance.uploadMap(mapData, zipFilename, author, true));
      assertThat(result, is(ApiExceptionWithMultipleCodes.apiExceptionWithCode(
        ErrorCode.MAP_NAME_MISSING,
        ErrorCode.MAP_DESCRIPTION_MISSING,
        ErrorCode.MAP_FIRST_TEAM_FFA,
        ErrorCode.MAP_TYPE_MISSING,
        ErrorCode.MAP_SIZE_MISSING,
        ErrorCode.MAP_VERSION_MISSING)));
    }
    verify(mapRepository, never()).save(any(com.faforever.api.data.domain.Map.class));
  }

  @Test
  void mapWithMoreRootFoldersInZip() throws IOException {
    String zipFilename = "scmp_037_invalid.zip";
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
  void uploadMapWithInvalidCharactersName() throws IOException {
    String zipFilename = "scmp_037_no_ascii.zip";
    try (InputStream inputStream = loadMapResourceAsStream(zipFilename)) {
      byte[] mapData = ByteStreams.toByteArray(inputStream);

      try {
        instance.uploadMap(mapData, zipFilename, author, true);
      } catch (ApiException e) {
        assertThat(e, apiExceptionWithCode(ErrorCode.MAP_NAME_INVALID));
      }

      verify(mapRepository, never()).save(any(com.faforever.api.data.domain.Map.class));
    }
  }

  @Test
  void positiveUploadTest() throws Exception {
    String zipFilename = "scmp_037.zip";
    when(fafApiProperties.getMap()).thenReturn(mapProperties);
    when(mapRepository.findOneByDisplayName(any())).thenReturn(Optional.empty());
    try (InputStream inputStream = loadMapResourceAsStream(zipFilename)) {
      byte[] mapData = ByteStreams.toByteArray(inputStream);

      Path tmpDir = temporaryDirectory;
      instance.uploadMap(mapData, zipFilename, author, true);

      ArgumentCaptor<com.faforever.api.data.domain.Map> mapCaptor = ArgumentCaptor.forClass(com.faforever.api.data.domain.Map.class);
      verify(mapRepository, Mockito.times(1)).save(mapCaptor.capture());
      assertEquals("Sludge_Test &!$.#+/.", mapCaptor.getValue().getDisplayName());
      assertEquals("skirmish", mapCaptor.getValue().getMapType());
      assertEquals("FFA", mapCaptor.getValue().getBattleType());
      assertEquals(1, mapCaptor.getValue().getVersions().size());

      MapVersion mapVersion = mapCaptor.getValue().getVersions().get(0);
      assertEquals("The thick, brackish water clings to everything, staining anything it touches. If it weren't for this planet's proximity to the Quarantine Zone, no one would ever bother coming here.", mapVersion.getDescription());
      assertEquals(1, mapVersion.getVersion());
      assertEquals(256, mapVersion.getHeight());
      assertEquals(256, mapVersion.getWidth());
      assertEquals(3, mapVersion.getMaxPlayers());
      assertEquals("maps/sludge_test____.___..v0001.zip", mapVersion.getFilename());

      assertFalse(Files.exists(tmpDir));

      Path generatedFile = finalDirectory.resolve("sludge_test____.___..v0001.zip");
      assertTrue(Files.exists(generatedFile));

      Path generatedFiles = finalDirectory.resolve("generated_files");
      Unzipper.from(generatedFile).to(generatedFiles).unzip();

      Path expectedFiles = finalDirectory.resolve("expected_files");
      Unzipper.from(generatedFile)
        .to(expectedFiles)
        .unzip();

      expectedFiles = expectedFiles.resolve("sludge_test____.___..v0001");
      try (Stream<Path> fileStream = Files.list(expectedFiles)) {
        assertEquals(fileStream.count(), (long) 4);
      }

      try (Stream<Path> fileStream = Files.list(expectedFiles)) {
        Path finalGeneratedFile = generatedFiles.resolve("sludge_test____.___..v0001");
        fileStream.forEach(expectedFile ->
          FileAssert.assertEquals("Difference in " + expectedFile.getFileName().toString(),
            expectedFile.toFile(),
            finalGeneratedFile.resolve(expectedFile.getFileName().toString()).toFile())
        );

        assertTrue(Files.exists(mapProperties.getDirectoryPreviewPathLarge().resolve("sludge_test____.___..v0001.png")));
        assertTrue(Files.exists(mapProperties.getDirectoryPreviewPathSmall().resolve("sludge_test____.___..v0001.png")));
      }
    }
  }

  private InputStream loadMapResourceAsStream(String filename) {
    return MapServiceTest.class.getResourceAsStream("/maps/" + filename);
  }
}

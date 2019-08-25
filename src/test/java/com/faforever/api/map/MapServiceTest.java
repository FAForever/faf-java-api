package com.faforever.api.map;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.config.FafApiProperties.Map;
import com.faforever.api.content.ContentService;
import com.faforever.api.data.domain.MapVersion;
import com.faforever.api.data.domain.Player;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.ApiExceptionWithMultipleCodes;
import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import com.faforever.commons.io.Unzipper;
import com.google.common.io.ByteStreams;
import junitx.framework.FileAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.faforever.api.error.ApiExceptionWithCode.apiExceptionWithCode;
import static com.faforever.api.error.ErrorCode.MAP_NAME_DOES_NOT_START_WITH_LETTER;
import static com.faforever.api.error.ErrorCode.MAP_NAME_INVALID_CHARACTER;
import static com.faforever.api.error.ErrorCode.MAP_NAME_INVALID_MINUS_OCCURENCE;
import static com.faforever.api.error.ErrorCode.MAP_NAME_TOO_LONG;
import static com.faforever.api.error.ErrorCode.MAP_NAME_TOO_SHORT;
import static com.faforever.api.error.ErrorCode.MAP_SCRIPT_LINE_MISSING;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MapServiceTest {
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
  void beforeEach() {
    instance = new MapService(fafApiProperties, mapRepository, contentService);
  }

  @Nested
  class Validation {

    @ParameterizedTest
    @ValueSource(strings = {
      "Map1",
      "map-2",
      "A very but overall not really too long map name",
      "Three - dashes - are - allowed"
    })
    void testMapNameValid(String name) {
      MapValidationRequest request = new MapValidationRequest(name, null);
      instance.validate(request);
    }

    @Test
    void testMapNameMultiErrorsButNoScenarioValidation() {
      MapValidationRequest request = new MapValidationRequest("123Invalid-in$-many-ways-atOnce" + StringUtils.repeat("x", 50), "");
      ApiException result = assertThrows(ApiException.class, () -> instance.validate(request));
      List<ErrorCode> errorCodes = Arrays.stream(result.getErrors()).map(Error::getErrorCode).collect(Collectors.toList());
      assertThat(errorCodes, hasItems(MAP_NAME_INVALID_CHARACTER, MAP_NAME_TOO_LONG, MAP_NAME_INVALID_MINUS_OCCURENCE));
      assertThat(errorCodes, not(contains(MAP_SCRIPT_LINE_MISSING)));
    }

    @ParameterizedTest
    @ValueSource(strings = {
      "Map.With.Dots",
      "Map/With/Slashes",
      "Map,With,Commas",
      "Map|With|Pipes",
      "SomeMore:",
      "SomeMore(",
      "SomeMore)",
      "SomeMore[",
      "SomeMore]",
      "SomeMore?",
      "SomeMore$",
    })
    void testMapNameInvalidChars(String name) {
      MapValidationRequest request = new MapValidationRequest(name, null);
      ApiException result = assertThrows(ApiException.class, () -> instance.validate(request));
      assertThat(result, apiExceptionWithCode(MAP_NAME_INVALID_CHARACTER));
    }

    @Test
    void testMapNameTooManyDashes() {
      MapValidationRequest request = new MapValidationRequest("More-than-three-dashes-invalid", null);
      ApiException result = assertThrows(ApiException.class, () -> instance.validate(request));
      assertThat(result, apiExceptionWithCode(MAP_NAME_INVALID_MINUS_OCCURENCE));
    }

    @Test
    void testMapNameToShort() {
      MapValidationRequest request = new MapValidationRequest("x", null);
      ApiException result = assertThrows(ApiException.class, () -> instance.validate(request));
      assertThat(result, apiExceptionWithCode(MAP_NAME_TOO_SHORT));
    }

    @Test
    void testMapNameToLong() {
      MapValidationRequest request = new MapValidationRequest(StringUtils.repeat("x", 51), null);
      ApiException result = assertThrows(ApiException.class, () -> instance.validate(request));
      assertThat(result, apiExceptionWithCode(MAP_NAME_TOO_LONG));
    }

    @Test
    void testMapNameStartsInvalid() {
      MapValidationRequest request = new MapValidationRequest("123x", null);
      ApiException result = assertThrows(ApiException.class, () -> instance.validate(request));
      assertThat(result, apiExceptionWithCode(MAP_NAME_DOES_NOT_START_WITH_LETTER));
    }

    @Test
    void testScenarioLuaSuccessWithoutVersion() {
      MapValidationRequest request = new MapValidationRequest("name",
        "map = '/maps/name/name.scmap', " +
          "save = '/maps/name/name_save.lua', " +
          "script = '/maps/name/name_script.lua', "
      );

      instance.validate(request);
    }

    @Test
    void testScenarioLuaSuccessWithVersion() {
      // of course the version number should be the same every time but we don't validate this
      MapValidationRequest request = new MapValidationRequest("name",
        "map = '/maps/name.v0001/name.scmap', " +
          "save = '/maps/name.v0002/name_save.lua', " +
          "script = '/maps/name.v0003/name_script.lua', "
      );

      instance.validate(request);
    }

    @Test
    void testScenarioLuaMissingAllLines() {
      MapValidationRequest request = new MapValidationRequest("name", "");
      ApiException result = assertThrows(ApiException.class, () -> instance.validate(request));
      assertThat(result.getErrors().length, is(3));
    }

    @Test
    void testScenarioLuaWrongMapLine() {
      MapValidationRequest request = new MapValidationRequest("name",
        "map = '/maps/name.vINVALID_VERSION/name.scmap', " +
          "save = '/maps/name.v0002/name_save.lua', " +
          "script = '/maps/name.v0003/name_script.lua', "
      );

      ApiException result = assertThrows(ApiException.class, () -> instance.validate(request));

      assertThat(result.getErrors().length, is(1));
      assertThat(result.getErrors()[0].getArgs().length, is(1));
      assertThat(result.getErrors()[0].getArgs()[0], is("map = '/maps/name/name.scmap',"));
    }

    @Test
    void testScenarioLuaWrongSaveLine() {
      MapValidationRequest request = new MapValidationRequest("name",
        "map = '/maps/name.v0001/name.scmap', " +
          "save = '/maps/name.vINVALID_VERSION/name_save.lua', " +
          "script = '/maps/name.v0003/name_script.lua', "
      );

      ApiException result = assertThrows(ApiException.class, () -> instance.validate(request));

      assertThat(result.getErrors().length, is(1));
      assertThat(result.getErrors()[0].getArgs().length, is(1));
      assertThat(result.getErrors()[0].getArgs()[0], is("save = '/maps/name/name_save.lua',"));
    }

    @Test
    void testScenarioLuaWrongScriptLine() {
      MapValidationRequest request = new MapValidationRequest("name",
        "map = '/maps/name.v0001/name.scmap', " +
          "save = '/maps/name.v0002/name_save.lua', " +
          "script = '/maps/name.vINVALID_VERSION/name_script.lua', "
      );

      ApiException result = assertThrows(ApiException.class, () -> instance.validate(request));

      assertThat(result.getErrors().length, is(1));
      assertThat(result.getErrors()[0].getArgs().length, is(1));
      assertThat(result.getErrors()[0].getArgs()[0], is("script = '/maps/name/name_script.lua',"));
    }
  }

  @Nested
  class WithTempDir {
    @TempDir
    Path baseTemporaryDirectory;

    @BeforeEach
    void setUp() throws Exception {
      temporaryDirectory = Files.createDirectory(baseTemporaryDirectory.resolve("temp"));
      finalDirectory = Files.createDirectory(baseTemporaryDirectory.resolve("final"));

      mapProperties = new Map()
        .setTargetDirectory(finalDirectory)
        .setDirectoryPreviewPathLarge(finalDirectory.resolve("large"))
        .setDirectoryPreviewPathSmall(finalDirectory.resolve("small"));
      when(contentService.createTempDir()).thenReturn(temporaryDirectory);
    }

    @ParameterizedTest(name = "Expecting ErrorCode.{0} with file ''{1}''")
    @CsvSource(value = {
      "MAP_MISSING_MAP_FOLDER_INSIDE_ZIP,empty.zip",
      "MAP_FIRST_TEAM_FFA,wrong_team_name.zip",
      "MAP_INVALID_ZIP,scmp_037_invalid.zip", // map with more than 1 root folders in zip
      "MAP_NAME_INVALID_CHARACTER,scmp_037_no_ascii.zip",
      "MAP_FILE_INSIDE_ZIP_MISSING,without_savelua.zip",
      "MAP_FILE_INSIDE_ZIP_MISSING,without_scenariolua.zip",
      "MAP_FILE_INSIDE_ZIP_MISSING,without_scmap.zip",
      "MAP_FILE_INSIDE_ZIP_MISSING,without_scriptlua.zip",
    })
    void uploadFails(String errorCodeEnumValue, String fileName) throws IOException {
      uploadFails(ErrorCode.valueOf(errorCodeEnumValue), fileName);
    }

    void uploadFails(ErrorCode expectedErrorCode, String fileName) throws IOException {
      byte[] mapData = loadMapAsBytes(fileName);
      ApiException result = assertThrows(ApiException.class, () -> instance.uploadMap(mapData, fileName, author, true));
      assertThat(result, apiExceptionWithCode(expectedErrorCode));
      verify(mapRepository, never()).save(any(com.faforever.api.data.domain.Map.class));
    }


    @Test
    void zipFilenameAlreadyExists() throws IOException {
      Path clashedMap = finalDirectory.resolve("sludge_test____.___..v0001.zip");
      assertTrue(clashedMap.toFile().createNewFile());
      when(fafApiProperties.getMap()).thenReturn(mapProperties);
      when(mapRepository.findOneByDisplayName(any())).thenReturn(Optional.empty());

      uploadFails(ErrorCode.MAP_NAME_CONFLICT, "scmp_037.zip");
    }

    @Test
    void notCorrectAuthor() throws IOException {
      Player me = new Player();
      me.setId(1);
      Player bob = new Player();
      bob.setId(2);

      com.faforever.api.data.domain.Map map = new com.faforever.api.data.domain.Map().setAuthor(bob);
      when(mapRepository.findOneByDisplayName(any())).thenReturn(Optional.of(map));

      uploadFails(ErrorCode.MAP_NOT_ORIGINAL_AUTHOR, "scmp_037.zip");
    }

    @Test
    void versionExistsAlready() throws IOException {
      com.faforever.api.data.domain.Map map = new com.faforever.api.data.domain.Map()
        .setAuthor(author)
        .setVersions(Collections.singletonList(new MapVersion().setVersion(1)));

      when(mapRepository.findOneByDisplayName(any())).thenReturn(Optional.of(map));
      when(author.getId()).thenReturn(1);

      uploadFails(ErrorCode.MAP_VERSION_EXISTS, "scmp_037.zip");
    }

    @Test
    void invalidScenario() throws IOException {
      String zipFilename = "invalid_scenario.zip";
      byte[] mapData = loadMapAsBytes(zipFilename);
      ApiException result = assertThrows(ApiException.class, () -> instance.uploadMap(mapData, zipFilename, author, true));
      assertThat(result, is(ApiExceptionWithMultipleCodes.apiExceptionWithCode(
        ErrorCode.MAP_NAME_MISSING,
        ErrorCode.MAP_DESCRIPTION_MISSING,
        ErrorCode.MAP_FIRST_TEAM_FFA,
        ErrorCode.MAP_TYPE_MISSING,
        ErrorCode.MAP_SIZE_MISSING,
        ErrorCode.MAP_VERSION_MISSING)));
      verify(mapRepository, never()).save(any(com.faforever.api.data.domain.Map.class));
    }

    @Test
    void positiveUploadTest() throws Exception {
      String zipFilename = "scmp_037.zip";
      when(fafApiProperties.getMap()).thenReturn(mapProperties);
      when(mapRepository.findOneByDisplayName(any())).thenReturn(Optional.empty());
      byte[] mapData = loadMapAsBytes(zipFilename);

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

    private byte[] loadMapAsBytes(String filename) throws IOException {
      try (InputStream inputStream = MapServiceTest.class.getResourceAsStream("/maps/" + filename)) {
        return ByteStreams.toByteArray(inputStream);
      }
    }
  }
}

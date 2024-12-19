package com.faforever.api.map;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.config.FafApiProperties.Map;
import com.faforever.api.content.ContentService;
import com.faforever.api.content.LicenseRepository;
import com.faforever.api.data.domain.BanInfo;
import com.faforever.api.data.domain.BanLevel;
import com.faforever.api.data.domain.License;
import com.faforever.api.data.domain.MapVersion;
import com.faforever.api.data.domain.Player;
import com.faforever.api.error.ApiException;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException.Forbidden;
import org.thymeleaf.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.faforever.api.error.ApiExceptionMatcher.hasErrorCode;
import static com.faforever.api.error.ApiExceptionMatcher.hasErrorCodes;
import static com.faforever.api.error.ErrorCode.MAP_NAME_DOES_NOT_START_WITH_LETTER;
import static com.faforever.api.error.ErrorCode.MAP_NAME_INVALID_CHARACTER;
import static com.faforever.api.error.ErrorCode.MAP_NAME_INVALID_MINUS_OCCURENCE;
import static com.faforever.api.error.ErrorCode.MAP_NAME_TOO_LONG;
import static com.faforever.api.error.ErrorCode.MAP_NAME_TOO_SHORT;
import static com.faforever.api.error.ErrorCode.MAP_SCRIPT_LINE_MISSING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
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
  private LicenseRepository licenseRepository;
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
    instance = new MapService(fafApiProperties, mapRepository, licenseRepository, contentService);
  }

  private String loadMapAsString(String filename) throws IOException {
    return new String(loadMapAsBytes(filename), StandardCharsets.UTF_8);
  }

  private InputStream loadMapAsInputSteam(String filename) {
    return MapServiceTest.class.getResourceAsStream("/maps/" + filename);
  }

  private byte[] loadMapAsBytes(String filename) throws IOException {
    try (InputStream inputStream = MapServiceTest.class.getResourceAsStream("/maps/" + filename)) {
      return ByteStreams.toByteArray(inputStream);
    }
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
      instance.validateMapName(name);
    }

    @Test
    void testMapNameMultiErrorsButNoScenarioValidation() {
      String mapName = "123Invalid-in$-many-ways-atOnce" + StringUtils.repeat("x", 50);
      ApiException result = assertThrows(ApiException.class, () -> instance.validateMapName(mapName));
      List<ErrorCode> errorCodes = Arrays.stream(result.getErrors()).map(Error::getErrorCode).toList();
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
      ApiException result = assertThrows(ApiException.class, () -> instance.validateMapName(name));
      assertThat(result, hasErrorCode(MAP_NAME_INVALID_CHARACTER));
    }

    @Test
    void testMapNameTooManyDashes() {
      ApiException result = assertThrows(ApiException.class, () -> instance.validateMapName("More-than-three-dashes-invalid"));
      assertThat(result, hasErrorCode(MAP_NAME_INVALID_MINUS_OCCURENCE));
    }

    @Test
    void testMapNameToShort() {
      ApiException result = assertThrows(ApiException.class, () -> instance.validateMapName("x"));
      assertThat(result, hasErrorCode(MAP_NAME_TOO_SHORT));
    }

    @Test
    void testMapNameToLong() {
      ApiException result = assertThrows(ApiException.class, () -> instance.validateMapName(StringUtils.repeat("x", 51)));
      assertThat(result, hasErrorCode(MAP_NAME_TOO_LONG));
    }

    @Test
    void testMapNameStartsInvalid() {
      ApiException result = assertThrows(ApiException.class, () -> instance.validateMapName("123x"));
      assertThat(result, hasErrorCode(MAP_NAME_DOES_NOT_START_WITH_LETTER));
    }

    @Test
    void testScenarioLuaSuccessWithoutVersion() throws Exception {
      instance.validateScenarioLua(loadMapAsString("scenario/valid_without_version_scenario.lua"));
    }

    @Test
    void testScenarioLuaSuccessWithVersion() throws Exception {
      instance.validateScenarioLua(loadMapAsString("scenario/valid_with_version_scenario.lua"));
    }

    @Test
    void testScenarioLuaEmptyScenarioLua() {
      ApiException result = assertThrows(ApiException.class, () -> instance.validateScenarioLua(""));
      assertThat(result, hasErrorCodes(
        ErrorCode.PARSING_LUA_FILE_FAILED
      ));
    }

    @Test
    void testScenarioLuaMissingAllLines() {
      ApiException result = assertThrows(ApiException.class, () ->
        instance.validateScenarioLua(loadMapAsString("scenario/valid_empty.lua")));
      assertThat(result, hasErrorCodes(ErrorCode.MAP_NAME_MISSING));
    }

    @Test
    void testScenarioLuaWrongMapLine() {
      ApiException result = assertThrows(ApiException.class, () ->
        instance.validateScenarioLua(loadMapAsString("scenario/missing_map_variable_scenario.lua")));

      assertThat(result, hasErrorCodes(
        ErrorCode.MAP_SCRIPT_LINE_MISSING
      ));

      assertThat(result.getErrors().length, is(1));
      Error mapLineError = Arrays.stream(result.getErrors())
        .filter(error -> error.getErrorCode() == MAP_SCRIPT_LINE_MISSING)
        .findFirst().get();

      assertThat(mapLineError.getArgs().length, is(1));
      assertThat(mapLineError.getArgs()[0], is("map = '/maps/mirage/mirage.scmap'"));
    }

    @Test
    void testScenarioLuaMapVersionOutOfRange() {
      ApiException result = assertThrows(ApiException.class, () ->
        instance.validateScenarioLua(loadMapAsString("scenario/invalid_lua_map_version_scenario.lua")));

      assertThat(result, hasErrorCodes(
        ErrorCode.MAP_VERSION_INVALID_RANGE
      ));

      assertThat(result.getErrors().length, is(1));
      Error mapLineError = Arrays.stream(result.getErrors())
        .filter(error -> error.getErrorCode() == ErrorCode.MAP_VERSION_INVALID_RANGE)
        .findFirst().get();

      assertThat(mapLineError.getArgs().length, is(2));
    }

    @Test
    void authorBannedFromVault() {
      when(fafApiProperties.getMap()).thenReturn(new Map().setAllowedExtensions(Set.of("zip")));
      when(author.getActiveBanOf(BanLevel.VAULT)).thenReturn(Optional.of(
        new BanInfo()
          .setLevel(BanLevel.VAULT)
      ));

      InputStream mapData = loadMapAsInputSteam("command_conquer_rush.v0007.zip");
      assertThrows(Forbidden.class, () -> instance.uploadMap(mapData, "command_conquer_rush.v0007.zip", author, true, null));
      verify(mapRepository, never()).save(any(com.faforever.api.data.domain.Map.class));
    }

    @Test
    void notAllowedFileExtension() {
      when(fafApiProperties.getMap()).thenReturn(new Map().setAllowedExtensions(Set.of("7z")));

      InputStream mapData = loadMapAsInputSteam("command_conquer_rush.v0007.zip");
      assertThrows(ApiException.class, () -> instance.uploadMap(mapData, "command_conquer_rush.v0007.zip", author, true, null));
      verify(mapRepository, never()).save(any(com.faforever.api.data.domain.Map.class));
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
        .setDirectoryPreviewPathSmall(finalDirectory.resolve("small"))
        .setAllowedExtensions(Set.of("zip"));
      when(contentService.createTempDir()).thenReturn(temporaryDirectory);
    }

    @ParameterizedTest(name = "Expecting ErrorCode.{0} with file ''{1}''")
    @CsvSource(value = {
      "MAP_MISSING_MAP_FOLDER_INSIDE_ZIP,empty.zip",
      "MAP_FIRST_TEAM_FFA,wrong_team_name.zip",
      "MAP_INVALID_ZIP,invalid_zip.zip", // map with more than 1 root folders in zip
      "MAP_NAME_INVALID_CHARACTER,map_name_invalid_character.zip",
      "MAP_FILE_INSIDE_ZIP_MISSING,without_savelua.zip",
      "MAP_FILE_INSIDE_ZIP_MISSING,without_scenariolua.zip",
      "MAP_FILE_INSIDE_ZIP_MISSING,without_scmap.zip",
      "MAP_FILE_INSIDE_ZIP_MISSING,without_scriptlua.zip",
      "ZIP_BOMB_DETECTED,map_zip_bomb.zip",
    })
    void uploadFails(String errorCodeEnumValue, String fileName) {
      uploadFails(ErrorCode.valueOf(errorCodeEnumValue), fileName);
    }

    void uploadFails(ErrorCode expectedErrorCode, String fileName) {
      when(fafApiProperties.getMap()).thenReturn(mapProperties);
      InputStream mapData = loadMapAsInputSteam(fileName);
      ApiException result = assertThrows(ApiException.class, () -> instance.uploadMap(mapData, fileName, author, true, null));
      assertThat(result, hasErrorCode(expectedErrorCode));
      verify(mapRepository, never()).save(any(com.faforever.api.data.domain.Map.class));
    }

    @Test
    void zipFilenameAlreadyExists() throws IOException {
      when(fafApiProperties.getMap()).thenReturn(mapProperties);
      Path clashedMap = finalDirectory.resolve("command_conquer_rush.v0007.zip");
      assertTrue(clashedMap.toFile().createNewFile());

      uploadFails(ErrorCode.MAP_NAME_CONFLICT, "command_conquer_rush.v0007.zip");
    }

    @Test
    void notCorrectAuthor() {
      when(fafApiProperties.getMap()).thenReturn(mapProperties);

      Player me = new Player();
      me.setId(1);
      Player bob = new Player();
      bob.setId(2);

      com.faforever.api.data.domain.Map map = new com.faforever.api.data.domain.Map().setAuthor(bob);
      when(mapRepository.findOneByDisplayName(any())).thenReturn(Optional.of(map));

      uploadFails(ErrorCode.MAP_NOT_ORIGINAL_AUTHOR, "command_conquer_rush.v0007.zip");
    }

    @Test
    void annonymousAuthor() {
      when(fafApiProperties.getMap()).thenReturn(mapProperties);

      Player me = new Player();
      me.setId(1);

      com.faforever.api.data.domain.Map map = new com.faforever.api.data.domain.Map().setAuthor(null);
      when(mapRepository.findOneByDisplayName(any())).thenReturn(Optional.of(map));

      uploadFails(ErrorCode.MAP_NOT_ORIGINAL_AUTHOR, "command_conquer_rush.v0007.zip");
    }

    @Test
    void versionExistsAlready() {
      when(fafApiProperties.getMap()).thenReturn(mapProperties);

      com.faforever.api.data.domain.Map map = new com.faforever.api.data.domain.Map()
        .setDisplayName("someName")
        .setAuthor(author)
        .setVersions(List.of(new MapVersion().setVersion(7)));

      when(mapRepository.findOneByDisplayName(any())).thenReturn(Optional.of(map));

      uploadFails(ErrorCode.MAP_VERSION_EXISTS, "command_conquer_rush.v0007.zip");
    }

    @Test
    void noMapName() {
      when(fafApiProperties.getMap()).thenReturn(mapProperties);
      String zipFilename = "no_map_name.zip";
      InputStream mapData = loadMapAsInputSteam(zipFilename);
      ApiException result = assertThrows(ApiException.class, () -> instance.uploadMap(mapData, zipFilename, author, true, null));
      assertThat(result, hasErrorCodes(ErrorCode.MAP_NAME_MISSING));
      verify(mapRepository, never()).save(any(com.faforever.api.data.domain.Map.class));
    }

    @Test
    void adaptiveFilesMissing() {
      when(fafApiProperties.getMap()).thenReturn(mapProperties);
      String zipFilename = "adaptive_map_files_missing.zip";
      InputStream mapData = loadMapAsInputSteam(zipFilename);
      ApiException result = assertThrows(ApiException.class, () -> instance.uploadMap(mapData, zipFilename, author, true, null));
      assertThat(result, hasErrorCodes(
        ErrorCode.MAP_FILE_INSIDE_ZIP_MISSING,
        ErrorCode.MAP_FILE_INSIDE_ZIP_MISSING
      ));
      verify(mapRepository, never()).save(any(com.faforever.api.data.domain.Map.class));
    }

    @Test
    void invalidScenario() {
      when(fafApiProperties.getMap()).thenReturn(mapProperties);
      String zipFilename = "invalid_scenario.zip";
      InputStream mapData = loadMapAsInputSteam(zipFilename);
      ApiException result = assertThrows(ApiException.class, () -> instance.uploadMap(mapData, zipFilename, author, true, null));
      assertThat(result, hasErrorCodes(
        ErrorCode.MAP_SCRIPT_LINE_MISSING,
        ErrorCode.MAP_SCRIPT_LINE_MISSING,
        ErrorCode.MAP_SCRIPT_LINE_MISSING,
        ErrorCode.MAP_DESCRIPTION_MISSING,
        ErrorCode.MAP_FIRST_TEAM_FFA,
        ErrorCode.MAP_TYPE_MISSING,
        ErrorCode.MAP_SIZE_MISSING,
        ErrorCode.MAP_VERSION_MISSING,
        ErrorCode.NO_RUSH_RADIUS_MISSING));
      verify(mapRepository, never()).save(any(com.faforever.api.data.domain.Map.class));
    }

    @Test
    void positiveUploadTest() throws Exception {
      String zipFilename = "command_conquer_rush.v0007.zip";
      License defaultLicense = new License().setId(1);
      when(fafApiProperties.getMap()).thenReturn(mapProperties);
      when(mapRepository.findOneByDisplayName(any())).thenReturn(Optional.empty());
      when(licenseRepository.findById(anyInt())).thenReturn(Optional.of(defaultLicense));
      InputStream mapData = loadMapAsInputSteam(zipFilename);

      Path tmpDir = temporaryDirectory;
      instance.uploadMap(mapData, zipFilename, author, true, 1);

      ArgumentCaptor<com.faforever.api.data.domain.Map> mapCaptor = ArgumentCaptor.forClass(com.faforever.api.data.domain.Map.class);
      verify(mapRepository).save(mapCaptor.capture());
      assertEquals("Command Conquer Rush", mapCaptor.getValue().getDisplayName());
      assertEquals("skirmish", mapCaptor.getValue().getMapType());
      assertEquals("FFA", mapCaptor.getValue().getBattleType());
      assertEquals(1, mapCaptor.getValue().getVersions().size());

      MapVersion mapVersion = mapCaptor.getValue().getVersions().get(0);
      assertEquals("For example on map crazyrush. Universal Command Conquer 3 modification by RuCommunity. Prealpha test", mapVersion.getDescription());
      assertEquals(7, mapVersion.getVersion());
      assertEquals(256, mapVersion.getHeight());
      assertEquals(256, mapVersion.getWidth());
      assertEquals(8, mapVersion.getMaxPlayers());
      assertEquals("maps/command_conquer_rush.v0007.zip", mapVersion.getFilename());

      assertFalse(Files.exists(tmpDir));

      Path generatedFile = finalDirectory.resolve("command_conquer_rush.v0007.zip");
      assertTrue(Files.exists(generatedFile));

      Path generatedFiles = finalDirectory.resolve("generated_files");
      Unzipper.from(generatedFile).to(generatedFiles).unzip();

      Path expectedFiles = finalDirectory.resolve("expected_files");
      Unzipper.from(generatedFile)
        .to(expectedFiles)
        .unzip();

      expectedFiles = expectedFiles.resolve("command_conquer_rush.v0007");
      try (Stream<Path> fileStream = Files.list(expectedFiles)) {
        assertEquals(fileStream.count(), 4);
      }

      try (Stream<Path> fileStream = Files.list(expectedFiles)) {
        Path finalGeneratedFile = generatedFiles.resolve("command_conquer_rush.v0007");
        fileStream.forEach(expectedFile ->
          FileAssert.assertEquals("Difference in " + expectedFile.getFileName().toString(),
            expectedFile.toFile(),
            finalGeneratedFile.resolve(expectedFile.getFileName().toString()).toFile())
        );

        assertTrue(Files.exists(mapProperties.getDirectoryPreviewPathLarge().resolve("command_conquer_rush.v0007.png")));
        assertTrue(Files.exists(mapProperties.getDirectoryPreviewPathSmall().resolve("command_conquer_rush.v0007.png")));
      }
    }
  }
}

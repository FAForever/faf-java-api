package com.faforever.api.mod;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.content.LicenseRepository;
import com.faforever.api.data.domain.BanInfo;
import com.faforever.api.data.domain.BanLevel;
import com.faforever.api.data.domain.License;
import com.faforever.api.data.domain.Mod;
import com.faforever.api.data.domain.ModVersion;
import com.faforever.api.data.domain.Player;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.ErrorCode;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;
import org.springframework.web.client.HttpClientErrorException.Forbidden;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.faforever.api.error.ApiExceptionMatcher.hasErrorCode;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ModServiceTest {

  public static final String TEST_MOD_FILENAME = "No Friendly Fire.zip";
  private static final String TEST_MOD = "/mods/" + TEST_MOD_FILENAME;
  public static final String TEST_MOD_INVALID_FILE_NAME = "Mod with top level files.zip";
  private static final String TEST_MOD_INVALID_STRUCTURE = "/mods/" + TEST_MOD_INVALID_FILE_NAME;

  @TempDir
  public Path temporaryFolder;

  private ModService instance;

  @Mock
  private ModRepository modRepository;
  @Mock
  private ModVersionRepository modVersionRepository;
  @Mock
  private LicenseRepository licenseRepository;

  @BeforeEach
  public void setUp() {
    FafApiProperties properties = new FafApiProperties();
    properties.getMod().setTargetDirectory(temporaryFolder.resolve("mods"));
    properties.getMod().setThumbnailTargetDirectory(temporaryFolder.resolve("thumbnails"));

    instance = new ModService(properties, modRepository, modVersionRepository, licenseRepository);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void processUploadedMod() throws Exception {
    Path uploadFile = prepareMod(TEST_MOD);

    Player uploader = new Player();

    when(modRepository.save(any(Mod.class))).thenAnswer(invocation -> invocation.getArgument(0));
    License fallBackLicense = new License().setId(1);
    when(licenseRepository.findById(anyInt())).thenReturn(Optional.of(fallBackLicense));

    String repositoryUrl = "https://gitlab.com/user/repo";

    instance.processUploadedMod(uploadFile, TEST_MOD_FILENAME, uploader, 1, repositoryUrl);

    assertThat(Files.exists(temporaryFolder.resolve("mods/no_friendly_fire.v0003.zip")), is(true));
    assertThat(Files.exists(temporaryFolder.resolve("thumbnails/no_friendly_fire.v0003.png")), is(true));

    ArgumentCaptor<Mod> modCaptor = ArgumentCaptor.forClass(Mod.class);
    verify(modRepository).save(modCaptor.capture());
    Mod savedMod = modCaptor.getValue();

    assertThat(savedMod.getId(), is(nullValue()));
    assertThat(savedMod.getAuthor(), is("IceDreamer"));
    assertThat(savedMod.getDisplayName(), is("No Friendly Fire"));
    assertThat(savedMod.getUploader(), is(uploader));
    assertThat(savedMod.getRepositoryUrl(), is(repositoryUrl));

    ModVersion savedModVersion = savedMod.getVersions().get(0);

    assertThat(savedModVersion.getId(), is(nullValue()));
    assertThat(savedModVersion.getIcon(), is("no_friendly_fire.v0003.png"));
    assertThat(savedModVersion.getFilename(), is("mods/no_friendly_fire.v0003.zip"));
    assertThat(savedModVersion.getUid(), is("26778D4E-BA75-5CC2-CBA8-63795BDE74AA"));
    assertThat(savedModVersion.getDescription(), is("All friendly fire, including between allies, is turned off."));
    assertThat(savedModVersion.getMod(), is(savedMod));
    assertThat(savedModVersion.isRanked(), is(false));
    assertThat(savedModVersion.isHidden(), is(false));

    ArgumentCaptor<Example<ModVersion>> exampleCaptor = ArgumentCaptor.forClass((Class) Example.class);
    verify(modVersionRepository).exists(exampleCaptor.capture());
    verify(modVersionRepository).existsByUid("26778D4E-BA75-5CC2-CBA8-63795BDE74AA");
  }

  @Test
  public void testExistingUid() throws Exception {
    Path uploadFile = prepareMod(TEST_MOD);

    when(modVersionRepository.existsByUid("26778D4E-BA75-5CC2-CBA8-63795BDE74AA")).thenReturn(true);

    ApiException result = assertThrows(ApiException.class, () -> instance.processUploadedMod(uploadFile, TEST_MOD_FILENAME, new Player(), null, null));
    assertThat(result, hasErrorCode(ErrorCode.MOD_UID_EXISTS));
  }

  @Test
  public void testUploaderVaultBanned() throws Exception {
    Path uploadFile = prepareMod(TEST_MOD);

    Player uploader = mock(Player.class);
    when(uploader.getActiveBanOf(BanLevel.VAULT)).thenReturn(Optional.of(
      new BanInfo()
        .setLevel(BanLevel.VAULT)
    ));

    assertThrows(Forbidden.class, () -> instance.processUploadedMod(uploadFile, TEST_MOD_FILENAME, uploader, null, null));
  }

  @Test
  public void testNotOriginalUploader() throws Exception {
    Path uploadFile = prepareMod(TEST_MOD);

    Player uploader = new Player();
    when(modRepository.existsByDisplayNameAndUploaderIsNot("No Friendly Fire", uploader)).thenReturn(true);
    when(modRepository.findOneByDisplayName("No Friendly Fire")).thenReturn(Optional.of(new Mod()));

    ApiException result = assertThrows(ApiException.class, () -> instance.processUploadedMod(uploadFile, TEST_MOD_FILENAME, uploader, null, null));
    assertThat(result, hasErrorCode(ErrorCode.MOD_NOT_ORIGINAL_AUTHOR));
  }

  @Test
  public void testNotAllowedRepositoryUrl() throws Exception {
    Path uploadFile = prepareMod(TEST_MOD);

    ApiException result = assertThrows(ApiException.class, () -> instance.processUploadedMod(uploadFile, TEST_MOD_FILENAME, new Player(), null, "https://badbadhost.bad/mod"));
    assertThat(result, hasErrorCode(ErrorCode.NOT_ALLOWED_URL_HOST));
  }

  @Test
  public void testMalformedRepositoryUrl() throws Exception {
    Path uploadFile = prepareMod(TEST_MOD);

    ApiException result = assertThrows(ApiException.class, () -> instance.processUploadedMod(uploadFile, TEST_MOD_FILENAME, new Player(), null, "host/mod"));
    assertThat(result, hasErrorCode(ErrorCode.MALFORMED_URL));
  }

  @Test
  public void testInvalidFileStructure() throws Exception {
    Path uploadFile = prepareMod(TEST_MOD_INVALID_STRUCTURE);
    ApiException result = assertThrows(ApiException.class, () -> instance.processUploadedMod(uploadFile, TEST_MOD_INVALID_FILE_NAME, new Player(), null, null));
    assertThat(result, hasErrorCode(ErrorCode.MOD_STRUCTURE_INVALID));
  }

  @Test
  public void testDisplayNameMissing() throws Exception {
    Path uploadFile = prepareModDynamic(luaContent().setName(null));
    ApiException result = assertThrows(ApiException.class, () -> instance.processUploadedMod(uploadFile, TEST_MOD_INVALID_FILE_NAME, new Player(), null, null));
    assertThat(result, hasErrorCode(ErrorCode.MOD_NAME_MISSING));
  }

  @Test
  public void testDisplayNameTooLong() throws Exception {
    Path uploadFile = prepareModDynamic(luaContent().setName(randomAlphanumeric(111)));
    ApiException result = assertThrows(ApiException.class, () -> instance.processUploadedMod(uploadFile, TEST_MOD_FILENAME, new Player(), null, null));
    assertThat(result, hasErrorCode(ErrorCode.MOD_NAME_TOO_LONG));
  }

  @Test
  public void testDisplayNameTooShort() throws Exception {
    Path uploadFile = prepareModDynamic(luaContent().setName(randomAlphanumeric(2)));
    ApiException result = assertThrows(ApiException.class, () -> instance.processUploadedMod(uploadFile, TEST_MOD_FILENAME, new Player(), null, null));
    assertThat(result, hasErrorCode(ErrorCode.MOD_NAME_TOO_SHORT));
  }

  @Test
  public void testDisplayNameInvalidContent() throws Exception {
    Path uploadFile = prepareModDynamic(luaContent().setName("A função, Ãugent"));
    ApiException result = assertThrows(ApiException.class, () -> instance.processUploadedMod(uploadFile, TEST_MOD_FILENAME, new Player(), null, null));
    assertThat(result, hasErrorCode(ErrorCode.MOD_NAME_INVALID));
  }

  @Test
  public void testUidMissing() throws Exception {
    Path uploadFile = prepareModDynamic(luaContent().setUid(null));
    ApiException result = assertThrows(ApiException.class, () -> instance.processUploadedMod(uploadFile, TEST_MOD_FILENAME, new Player(), null, null));
    assertThat(result, hasErrorCode(ErrorCode.MOD_UID_MISSING));
  }

  @Test
  public void testVersionMissing() throws Exception {
    Path uploadFile = prepareModDynamic(luaContent().setVersion(null));
    ApiException result = assertThrows(ApiException.class, () -> instance.processUploadedMod(uploadFile, TEST_MOD_FILENAME, new Player(), null, null));
    assertThat(result, hasErrorCode(ErrorCode.MOD_VERSION_MISSING));
  }

  @Test
  public void testVersionNotANumber() throws Exception {
    Path uploadFile = prepareModDynamic(luaContent().setVersion("NotANumber"));
    ApiException result = assertThrows(ApiException.class, () -> instance.processUploadedMod(uploadFile, TEST_MOD_FILENAME, new Player(), null, null));
    assertThat(result, hasErrorCode(ErrorCode.MOD_VERSION_NOT_A_NUMBER));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 10_000})
  public void testVersionOutOfRange(final int testVersion) throws Exception {
    Path uploadFile = prepareModDynamic(luaContent().setVersion(Integer.toString(testVersion)));
    ApiException result = assertThrows(ApiException.class, () -> instance.processUploadedMod(uploadFile, TEST_MOD_FILENAME, new Player(), null, null));
    assertThat(result, hasErrorCode(ErrorCode.MOD_VERSION_INVALID_RANGE));
  }

  @Test
  public void testDescriptionMissing() throws Exception {
    Path uploadFile = prepareModDynamic(luaContent().setDesc(null));
    ApiException result = assertThrows(ApiException.class, () -> instance.processUploadedMod(uploadFile, TEST_MOD_FILENAME, new Player(), null, null));
    assertThat(result, hasErrorCode(ErrorCode.MOD_DESCRIPTION_MISSING));
  }

  @Test
  public void testAuthorMissing() throws Exception {
    Path uploadFile = prepareModDynamic(luaContent().setAuthor(null));
    ApiException result = assertThrows(ApiException.class, () -> instance.processUploadedMod(uploadFile, TEST_MOD_FILENAME, new Player(), null, null));
    assertThat(result, hasErrorCode(ErrorCode.MOD_AUTHOR_MISSING));
  }

  @NotNull
  private LuaContent luaContent() {
    return new LuaContent()
      .setName(randomAlphanumeric(50))
      .setVersion("3")
      .setAuthor("The Author")
      .setCopyright("The Copyright")
      .setDesc("This is the description of a valid Lua Content.")
      .setUid("26778D4E-BA75-5CC2-CBA8-63795BDE74AA")
      .setExclusive(false)
      .setUiOnly(false)
      .setIconPath("/mods/foobar/mod_icon.png");
  }

  @NotNull
  private Path prepareMod(String path) throws IOException {
    Path uploadFile = temporaryFolder.resolve("uploaded-mod.zip");
    try (InputStream inputStream = new BufferedInputStream(getClass().getResourceAsStream(path))) {
      Files.copy(inputStream, uploadFile);
    }
    return uploadFile;
  }

  @NotNull
  private Path prepareModDynamic(LuaContent lc) throws IOException {
    Path uploadFile = temporaryFolder.resolve("uploaded-dynamic-mod.zip");
    try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(uploadFile))) {
      zos.putNextEntry(new ZipEntry("foobar/mod_info.lua"));
      zos.write(lc.asLuaString().getBytes());
      zos.closeEntry();
    }
    return uploadFile;
  }

}

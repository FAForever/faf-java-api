package com.faforever.api.mod;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.BanInfo;
import com.faforever.api.data.domain.BanLevel;
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

import static com.faforever.api.error.ApiExceptionMatcher.hasErrorCode;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ModServiceTest {

  private static final String TEST_MOD = "/mods/No Friendly Fire.zip";
  private static final String TEST_MOD_INVALID_STRUCTURE = "/mods/Mod with top level files.zip";

  @TempDir
  public Path temporaryFolder;

  private ModService instance;

  @Mock
  private ModRepository modRepository;
  @Mock
  private ModVersionRepository modVersionRepository;

  @BeforeEach
  public void setUp() {
    FafApiProperties properties = new FafApiProperties();
    properties.getMod().setTargetDirectory(temporaryFolder.resolve("mods"));
    properties.getMod().setThumbnailTargetDirectory(temporaryFolder.resolve("thumbnails"));

    instance = new ModService(properties, modRepository, modVersionRepository);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void processUploadedMod() throws Exception {
    Path uploadedFile = prepareMod(TEST_MOD);

    Player uploader = new Player();

    when(modRepository.save(any(Mod.class))).thenAnswer(invocation -> invocation.getArgument(0));

    instance.processUploadedMod(uploadedFile, uploader);

    assertThat(Files.exists(temporaryFolder.resolve("mods/no_friendly_fire.v0003.zip")), is(true));
    assertThat(Files.exists(temporaryFolder.resolve("thumbnails/no_friendly_fire.v0003.png")), is(true));

    ArgumentCaptor<Mod> modCaptor = ArgumentCaptor.forClass(Mod.class);
    verify(modRepository).save(modCaptor.capture());
    Mod savedMod = modCaptor.getValue();

    assertThat(savedMod.getId(), is(nullValue()));
    assertThat(savedMod.getAuthor(), is("IceDreamer"));
    assertThat(savedMod.getDisplayName(), is("No Friendly Fire"));
    assertThat(savedMod.getUploader(), is(uploader));

    ModVersion savedModVersion = savedMod.getVersions().get(0);

    assertThat(savedModVersion.getId(), is(nullValue()));
    assertThat(savedModVersion.getIcon(), is("no_friendly_fire.v0003.png"));
    assertThat(savedModVersion.getFilename(), is("mods/no_friendly_fire.v0003.zip"));
    assertThat(savedModVersion.getUid(), is("26778D4E-BA75-5CC2-CBA8-63795BDE74AA"));
    assertThat(savedModVersion.getDescription(), is("All friendly fire, including between allies, is turned off."));
    assertThat(savedModVersion.getMod(), is(savedMod));
    assertThat(savedModVersion.isRanked(), is(false));
    assertThat(savedModVersion.isHidden(), is(false));

    ArgumentCaptor<Example<ModVersion>> exampleCaptor = ArgumentCaptor.forClass((Class) ModVersion.class);
    verify(modVersionRepository).exists(exampleCaptor.capture());
    verify(modVersionRepository).existsByUid("26778D4E-BA75-5CC2-CBA8-63795BDE74AA");
  }

  @Test
  public void testExistingUid() throws Exception {
    Path uploadedFile = prepareMod(TEST_MOD);

    when(modVersionRepository.existsByUid("26778D4E-BA75-5CC2-CBA8-63795BDE74AA")).thenReturn(true);

    ApiException result = assertThrows(ApiException.class, () -> instance.processUploadedMod(uploadedFile, new Player()));
    assertThat(result, hasErrorCode(ErrorCode.MOD_UID_EXISTS));
  }

  @Test
  public void testUploaderVaultBanned() throws Exception {
    Path uploadedFile = prepareMod(TEST_MOD);

    Player uploader = mock(Player.class);
    when(uploader.getActiveBanOf(BanLevel.VAULT)).thenReturn(Optional.of(
      new BanInfo()
        .setLevel(BanLevel.VAULT)
    ));


    assertThrows(Forbidden.class, () -> instance.processUploadedMod(uploadedFile, uploader));
  }

  @Test
  public void testNotOriginalUploader() throws Exception {
    Path uploadedFile = prepareMod(TEST_MOD);

    Player uploader = new Player();
    when(modRepository.existsByDisplayNameAndUploaderIsNot("No Friendly Fire", uploader)).thenReturn(true);
    when(modRepository.findOneByDisplayName("No Friendly Fire")).thenReturn(Optional.of(new Mod()));

    ApiException result = assertThrows(ApiException.class, () -> instance.processUploadedMod(uploadedFile, uploader));
    assertThat(result, hasErrorCode(ErrorCode.MOD_NOT_ORIGINAL_AUTHOR));
  }

  @Test
  public void testInvalidFileStructure() throws Exception {
    Path uploadedFile = prepareMod(TEST_MOD_INVALID_STRUCTURE);

    Player uploader = new Player();

    ApiException result = assertThrows(ApiException.class, () -> instance.processUploadedMod(uploadedFile, uploader));
    assertThat(result, hasErrorCode(ErrorCode.MOD_STRUCTURE_INVALID));
  }

  @NotNull
  private Path prepareMod(String path) throws IOException {
    Path uploadedFile = temporaryFolder.resolve("uploaded-mod.zip");
    try (InputStream inputStream = new BufferedInputStream(getClass().getResourceAsStream(path))) {
      Files.copy(inputStream, uploadedFile);
    }
    return uploadedFile;
  }

  // TODO test error cases
}

package com.faforever.api.mod;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.BanInfo;
import com.faforever.api.data.domain.BanLevel;
import com.faforever.api.data.domain.Mod;
import com.faforever.api.data.domain.ModVersion;
import com.faforever.api.data.domain.Player;
import com.faforever.api.error.ApiExceptionMatcher;
import com.faforever.api.error.ErrorCode;
import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.migrationsupport.rules.ExpectedExceptionSupport;
import org.junit.jupiter.migrationsupport.rules.ExternalResourceSupport;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
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
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, ExternalResourceSupport.class, ExpectedExceptionSupport.class})
public class ModServiceTest {

  private static final String TEST_MOD = "/mods/No Friendly Fire.zip";
  private static final String TEST_MOD_INVALID_STRUCTURE = "/mods/Mod with top level files.zip";

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private ModService instance;

  @Mock
  private ModRepository modRepository;
  @Mock
  private ModVersionRepository modVersionRepository;

  @BeforeEach
  public void setUp() throws Exception {
    FafApiProperties properties = new FafApiProperties();
    properties.getMod().setTargetDirectory(temporaryFolder.getRoot().toPath().resolve("mods"));
    properties.getMod().setThumbnailTargetDirectory(temporaryFolder.getRoot().toPath().resolve("thumbnails"));

    instance = new ModService(properties, modRepository, modVersionRepository);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void processUploadedMod() throws Exception {
    Path uploadedFile = prepareMod(TEST_MOD);

    Player uploader = new Player();

    when(modRepository.save(any(Mod.class))).thenAnswer(invocation -> invocation.getArgument(0));

    instance.processUploadedMod(uploadedFile, uploader);

    assertThat(Files.exists(temporaryFolder.getRoot().toPath().resolve("mods/no_friendly_fire.v0003.zip")), is(true));
    assertThat(Files.exists(temporaryFolder.getRoot().toPath().resolve("thumbnails/no_friendly_fire.v0003.png")), is(true));

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
    expectedException.expect(ApiExceptionMatcher.hasErrorCode(ErrorCode.MOD_UID_EXISTS));

    instance.processUploadedMod(uploadedFile, new Player());
  }

  @Test
  public void testUploaderVaultBanned() throws Exception {
    Path uploadedFile = prepareMod(TEST_MOD);

    Player uploader = mock(Player.class);
    when(uploader.getActiveBans()).thenReturn(Set.of(
      new BanInfo()
        .setLevel(BanLevel.VAULT)
    ));

    expectedException.expect(Forbidden.class);

    instance.processUploadedMod(uploadedFile, uploader);
  }

  @Test
  public void testNotOriginalUploader() throws Exception {
    Path uploadedFile = prepareMod(TEST_MOD);

    Player uploader = new Player();
    when(modRepository.existsByDisplayNameAndUploaderIsNot("No Friendly Fire", uploader)).thenReturn(true);
    when(modRepository.findOneByDisplayName("No Friendly Fire")).thenReturn(Optional.of(new Mod()));

    expectedException.expect(ApiExceptionMatcher.hasErrorCode(ErrorCode.MOD_NOT_ORIGINAL_AUTHOR));

    instance.processUploadedMod(uploadedFile, uploader);
  }

  @Test
  public void testInvalidFileStructure() throws Exception {
    Path uploadedFile = prepareMod(TEST_MOD_INVALID_STRUCTURE);

    Player uploader = new Player();

    expectedException.expect(ApiExceptionMatcher.hasErrorCode(ErrorCode.MOD_STRUCTURE_INVALID));

    instance.processUploadedMod(uploadedFile, uploader);
  }

  @NotNull
  private Path prepareMod(String path) throws IOException {
    Path uploadedFile = temporaryFolder.getRoot().toPath().resolve("uploaded-mod.zip");
    try (InputStream inputStream = new BufferedInputStream(getClass().getResourceAsStream(path))) {
      Files.copy(inputStream, uploadedFile);
    }
    return uploadedFile;
  }

  // TODO test error cases
}

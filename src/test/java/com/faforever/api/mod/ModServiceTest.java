package com.faforever.api.mod;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.Mod;
import com.faforever.api.data.domain.ModVersion;
import com.faforever.api.data.domain.Player;
import com.faforever.api.error.ApiExceptionWithCode;
import com.faforever.api.error.ErrorCode;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Example;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ModServiceTest {

  private static final String TEST_MOD = "/mods/No Friendly Fire.zip";

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private ModService instance;

  @Mock
  private ModRepository modRepository;
  @Mock
  private ModVersionRepository modVersionRepository;

  @Before
  public void setUp() throws Exception {
    FafApiProperties properties = new FafApiProperties();
    properties.getMod().setTargetDirectory(temporaryFolder.getRoot().toPath().resolve("mods"));
    properties.getMod().setThumbnailTargetDirectory(temporaryFolder.getRoot().toPath().resolve("thumbnails"));

    when(modRepository.save(any(Mod.class))).thenAnswer(invocation -> invocation.getArgument(0));

    instance = new ModService(properties, modRepository, modVersionRepository);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void processUploadedMod() throws Exception {
    Path uploadedFile = prepareMod();

    Player uploader = new Player();

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
    Path uploadedFile = prepareMod();

    when(modVersionRepository.existsByUid("26778D4E-BA75-5CC2-CBA8-63795BDE74AA")).thenReturn(true);
    expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.MOD_UID_EXISTS));

    instance.processUploadedMod(uploadedFile, new Player());
  }

  @Test
  public void testNotOriginalUploader() throws Exception {
    Path uploadedFile = prepareMod();

    Player uploader = new Player();
    when(modRepository.existsByDisplayNameIgnoreCaseAndUploaderIsNot("No Friendly Fire", uploader)).thenReturn(true);
    when(modRepository.findOneByDisplayName("No Friendly Fire")).thenReturn(Optional.of(new Mod()));

    expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.MOD_NOT_ORIGINAL_AUTHOR));

    instance.processUploadedMod(uploadedFile, uploader);
  }

  @NotNull
  private Path prepareMod() throws IOException {
    Path uploadedFile = temporaryFolder.getRoot().toPath().resolve("uploaded-mod.zip");
    try (InputStream inputStream = new BufferedInputStream(getClass().getResourceAsStream(TEST_MOD))) {
      Files.copy(inputStream, uploadedFile);
    }
    return uploadedFile;
  }

  // TODO test error cases
}

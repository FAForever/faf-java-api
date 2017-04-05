package com.faforever.api.mod;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.Mod;
import com.faforever.api.data.domain.ModVersion;
import com.faforever.api.data.domain.Player;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

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
  public void processUploadedMod() throws Exception {
    Path uploadedFile = temporaryFolder.getRoot().toPath().resolve("uploaded-mod.zip");
    try (InputStream inputStream = new BufferedInputStream(getClass().getResourceAsStream(TEST_MOD))) {
      Files.copy(inputStream, uploadedFile);
    }

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

    ArgumentCaptor<ModVersion> versionCaptor = ArgumentCaptor.forClass(ModVersion.class);
    verify(modVersionRepository).save(versionCaptor.capture());
    ModVersion savedModVersion = versionCaptor.getValue();

    assertThat(savedModVersion.getId(), is(nullValue()));
    assertThat(savedModVersion.getIcon(), is("no_friendly_fire.v0003.png"));
    assertThat(savedModVersion.getFilename(), is("mods/no_friendly_fire.v0003.zip"));
    assertThat(savedModVersion.getUid(), is("26778D4E-BA75-5CC2-CBA8-63795BDE74AA"));
    assertThat(savedModVersion.getDescription(), is("All friendly fire, including between allies, is turned off."));
    assertThat(savedModVersion.getMod(), is(savedMod));
    assertThat(savedModVersion.isRanked(), is(false));
    assertThat(savedModVersion.isHidden(), is(false));
  }

  // TODO test error cases
}

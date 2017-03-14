package com.faforever.api.deployment;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.config.FafApiProperties.Deployment;
import com.faforever.api.config.FafApiProperties.Deployment.DeploymentConfiguration;
import com.faforever.api.deployment.git.GitWrapper;
import com.faforever.api.featuredmods.FeaturedModFile;
import com.faforever.api.featuredmods.FeaturedModService;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LegacyFeaturedModDeploymentTaskTest {

  @Rule
  public TemporaryFolder repositoriesFolder = new TemporaryFolder();
  @Rule
  public TemporaryFolder targetFolder = new TemporaryFolder();
  private LegacyFeaturedModDeploymentTask instance;
  @Mock
  private GitWrapper gitWrapper;
  @Mock
  private FeaturedModService featuredModService;
  private FafApiProperties properties;

  @Before
  public void setUp() throws Exception {
    properties = new FafApiProperties();
    Deployment deployment = properties.getDeployment();
    deployment.setRepositoriesDirectory(repositoriesFolder.getRoot().getAbsolutePath());
    deployment.setFeaturedModsTargetDirectory(targetFolder.getRoot().getAbsolutePath());

    instance = new LegacyFeaturedModDeploymentTask(gitWrapper, featuredModService, properties);
  }

  @Test(expected = IllegalStateException.class)
  public void testRunWithoutConfigurationThrowsException() throws Exception {
    instance.run();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testRun() throws Exception {
    instance.setConfiguration(new DeploymentConfiguration()
        .setBranch("branch")
        .setModFilesExtension("nx3")
        .setModName("faf")
        .setReplaceExisting(true)
        .setRepositoryUrl("git@example.com/FAForever/faf"));

    Mockito.doAnswer(invocation -> {
      Path repoFolder = invocation.getArgumentAt(0, Path.class);
      Files.createDirectories(repoFolder.resolve("someDir"));
      Files.copy(
          LegacyFeaturedModDeploymentTaskTest.class.getResourceAsStream("/featured_mod/mod_info.lua"),
          repoFolder.resolve("mod_info.lua")
      );
      Files.copy(LegacyFeaturedModDeploymentTaskTest.class.getResourceAsStream("/featured_mod/someDir/someFile"),
          repoFolder.resolve("someDir/someFile")
      );
      return null;
    }).when(gitWrapper).checkoutRef(any(), any());

    when(featuredModService.getFileIds("faf")).thenReturn(ImmutableMap.of(
        "ForgedAlliance.exe", (short) 1,
        "someDir.nx3", (short) 2
    ));

    Path dummyExe = repositoriesFolder.getRoot().toPath().resolve("TemplateForgedAlliance.exe");
    createDummyExe(dummyExe);
    properties.getDeployment().setForgedAllianceExePath(dummyExe.toAbsolutePath().toString());

    instance.run();

    ArgumentCaptor<List<FeaturedModFile>> filesCaptor = ArgumentCaptor.forClass((Class) List.class);
    verify(featuredModService).save(eq("faf"), eq((short) 1337), filesCaptor.capture());

    List<FeaturedModFile> files = filesCaptor.getValue();
    files.sort(Comparator.comparing(FeaturedModFile::getFileId));

    assertThat(files.get(0).getFileId(), is(1));
    assertThat(files.get(0).getMd5(), is("47df959058cb52fe966ea5936dbd8f4c"));
    assertThat(files.get(0).getName(), is("ForgedAlliance.1337.exe"));
    assertThat(files.get(0).getVersion(), is((short) 1337));

    assertThat(files.get(1).getFileId(), is(2));
    assertThat(files.get(1).getMd5(), is(notNullValue()));
    assertThat(files.get(1).getName(), is("someDir.1337.nxt"));
    assertThat(files.get(1).getVersion(), is((short) 1337));

    assertThat(Files.exists(targetFolder.getRoot().toPath().resolve("updates_faf_files/someDir.1337.nxt")), is(true));
    assertThat(Files.exists(targetFolder.getRoot().toPath().resolve("updates_faf_files/ForgedAlliance.1337.exe")), is(true));
  }

  private void createDummyExe(Path file) throws IOException {
    try (RandomAccessFile randomAccessFile = new RandomAccessFile(file.toFile(), "rw")) {
      randomAccessFile.setLength(12_444_928);
    }
  }
}

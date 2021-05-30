package com.faforever.api.deployment;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.content.ContentService;
import com.faforever.api.error.ApiException;
import com.faforever.api.featuredmods.FeaturedModFile;
import com.faforever.api.featuredmods.FeaturedModService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExeUploaderServiceTest {
  private ExeUploaderService instance;

  private Path temporaryDirectory;
  private Path finalDirectory;

  @Mock
  private ContentService contentService;
  @Mock
  private FafApiProperties apiProperties;
  @Mock
  private FafApiProperties.Deployment deployment;
  @Mock
  private FeaturedModService featuredModService;
  private InputStream exeDataInputStream;

  private FeaturedModFile featuredModFile;

  @BeforeEach
  public void setUp() {
    instance = new ExeUploaderService(contentService, apiProperties, featuredModService);
    exeDataInputStream = new ByteArrayInputStream(new byte[]{1, 2, 3, 4});

    featuredModFile = new FeaturedModFile();
    featuredModFile.setName("ForgedAlliance.exe");
    featuredModFile.setVersion(1);
    featuredModFile.setFileId((short) 1);
  }

  @AfterEach
  public void after() {
    verifyNoMoreInteractions(contentService, apiProperties, deployment, featuredModService);
  }

  @Nested
  class WithTempDir {
    @TempDir
    public Path baseTemporaryDirectory;

    @BeforeEach
    public void setUp() throws IOException {
      temporaryDirectory = Files.createDirectory(baseTemporaryDirectory.resolve("temp"));
      finalDirectory = Files.createDirectory(baseTemporaryDirectory.resolve("final"));

      when(apiProperties.getDeployment()).thenReturn(deployment);
      when(contentService.createTempDir()).thenReturn(temporaryDirectory);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testProcessUploadBeta() {
      String modName = "fafbeta";

      String finalExeDestination = finalDirectory.toAbsolutePath() + "/ForgedAlliance.1.exe";
      when(deployment.getForgedAllianceBetaExePath()).thenReturn(finalExeDestination);

      when(featuredModService.getFile(modName, null, "ForgedAlliance.exe")).thenReturn(
        featuredModFile);
      instance.processUpload(exeDataInputStream, modName);

      assertTrue(Files.exists(Paths.get(finalExeDestination)));
      ArgumentCaptor<List<FeaturedModFile>> modFilesCaptor = ArgumentCaptor.forClass(List.class);
      verify(featuredModService).save(eq(modName), eq((short) featuredModFile.getVersion()), modFilesCaptor.capture());
      assertThat(modFilesCaptor.getValue().size(), is(1));
      assertThat(modFilesCaptor.getValue(), hasItem(featuredModFile));

      verify(contentService).createTempDir();
      verify(apiProperties, atLeastOnce()).getDeployment();
      verify(deployment).getForgedAllianceBetaExePath();
      verify(featuredModService).getFile(modName, null, "ForgedAlliance.exe");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testProcessUploadDevelop() {
      String modName = "fafdevelop";

      String finalExeDestination = finalDirectory.toAbsolutePath() + "/ForgedAlliance.1.exe";
      when(deployment.getForgedAllianceDevelopExePath()).thenReturn(finalExeDestination);

      when(featuredModService.getFile(modName, null, "ForgedAlliance.exe")).thenReturn(
        featuredModFile);
      instance.processUpload(exeDataInputStream, modName);

      assertTrue(Files.exists(Paths.get(finalExeDestination)));
      ArgumentCaptor<List<FeaturedModFile>> modFilesCaptor = ArgumentCaptor.forClass(List.class);
      verify(featuredModService).save(eq(modName), eq((short) featuredModFile.getVersion()), modFilesCaptor.capture());
      assertThat(modFilesCaptor.getValue().size(), is(1));
      assertThat(modFilesCaptor.getValue(), hasItem(featuredModFile));

      verify(contentService).createTempDir();
      verify(apiProperties, atLeastOnce()).getDeployment();
      verify(deployment).getForgedAllianceDevelopExePath();
      verify(featuredModService).getFile(modName, null, "ForgedAlliance.exe");
    }
  }

  @Test
  public void testProcessUploadIsForbidden() {
    String modName = "faf";
    Assertions.assertThrows(ApiException.class, () -> instance.processUpload(exeDataInputStream, modName)) ;
  }
}

package com.faforever.api.deployment;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.content.ContentService;
import com.faforever.api.error.ApiException;
import com.faforever.api.featuredmods.FeaturedModFile;
import com.faforever.api.featuredmods.FeaturedModService;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class) // Is this right?
public class ExeUploaderServiceTest {
  private ExeUploaderService instance;

  @Rule
  public final TemporaryFolder temporaryDirectory = new TemporaryFolder();
  @Rule
  public final TemporaryFolder finalDirectory = new TemporaryFolder();

  @Mock
  private ContentService contentService;
  @Mock
  private FafApiProperties apiProperties;
  @Mock
  private FafApiProperties.Deployment deployment;
  @Mock
  private FeaturedModService featuredModService;
  private byte[] bytes;


  private FeaturedModFile featuredModFile;

  @Before
  public void setUp() {
    instance = new ExeUploaderService(contentService, apiProperties, featuredModService);

    when(apiProperties.getDeployment()).thenReturn(deployment);
    bytes = new byte[]{1, 2, 3, 4};
    when(contentService.createTempDir()).thenReturn(temporaryDirectory.getRoot().toPath());

    featuredModFile = new FeaturedModFile();
    featuredModFile.setName("ForgedAlliance.exe");
    featuredModFile.setVersion(1);
    featuredModFile.setFileId((short) 1);
  }

  @After
  public void after() {
    verifyNoMoreInteractions(contentService, apiProperties, deployment, featuredModService);
  }

  @Test
  public void testProcessUploadBeta() {
    String modName = "fafbeta";

    String finalExeDestination = finalDirectory.getRoot().getAbsolutePath() + "/ForgedAlliance.1.exe";
    when(deployment.getForgedAllianceBetaExePath()).thenReturn(finalExeDestination);

    when(featuredModService.getFile(modName, null, "ForgedAlliance.exe")).thenReturn(
      featuredModFile);
    instance.processUpload(bytes, modName);

    // File was created
    assertTrue(Files.exists(Paths.get(finalExeDestination)));
    // File was updated in database
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
  public void testProcessUploadDevelop() {
    String modName = "fafdevelop";

    String finalExeDestination = finalDirectory.getRoot().getAbsolutePath() + "/ForgedAlliance.1.exe";
    when(deployment.getForgedAllianceDevelopExePath()).thenReturn(finalExeDestination);

    when(featuredModService.getFile(modName, null, "ForgedAlliance.exe")).thenReturn(
      featuredModFile);
    instance.processUpload(bytes, modName);

    // File was created
    assertTrue(Files.exists(Paths.get(finalExeDestination)));
    // File was updated in database
    ArgumentCaptor<List<FeaturedModFile>> modFilesCaptor = ArgumentCaptor.forClass(List.class);
    verify(featuredModService).save(eq(modName), eq((short) featuredModFile.getVersion()), modFilesCaptor.capture());
    assertThat(modFilesCaptor.getValue().size(), is(1));
    assertThat(modFilesCaptor.getValue(), hasItem(featuredModFile));

    verify(contentService).createTempDir();
    verify(apiProperties, atLeastOnce()).getDeployment();
    verify(deployment).getForgedAllianceBetaExePath();
    verify(deployment).getForgedAllianceDevelopExePath();
    verify(featuredModService).getFile(modName, null, "ForgedAlliance.exe");
  }

  @Test(expected = ApiException.class)
  public void testProcessUploadIsForbidden() {
    String modName = "faf";
    instance.processUpload(bytes, modName);
  }
}

package com.faforever.api.deployment;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.config.FafApiProperties.Deployment;
import com.faforever.api.data.domain.FeaturedMod;
import com.faforever.api.deployment.git.GitWrapper;
import com.faforever.api.featuredmods.FeaturedModFile;
import com.faforever.api.featuredmods.FeaturedModService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LegacyFeaturedModDeploymentTaskTest {

  @TempDir
  public Path repositoriesFolder;
  @TempDir
  public Path targetFolder;

  private LegacyFeaturedModDeploymentTask instance;
  @Mock
  private GitWrapper gitWrapper;
  @Mock
  private FeaturedModService featuredModService;
  @Mock
  private RestTemplate restTemplate;

  private FafApiProperties properties;

  @BeforeEach
  public void setUp() {
    properties = new FafApiProperties();
    Deployment deployment = properties.getDeployment();
    deployment.setRepositoriesDirectory(repositoriesFolder.toString());
    deployment.setFeaturedModsTargetDirectory(targetFolder.toString());

    instance = new LegacyFeaturedModDeploymentTask(gitWrapper, featuredModService, properties, restTemplate);
  }

  @Test
  public void testRunWithoutConfigurationThrowsException() {
    assertThrows(IllegalStateException.class, () -> instance.run() );
  }

  @Test
  public void testRunNoFileIds() throws Exception {
    instance.setFeaturedMod(new FeaturedMod()
      .setGitBranch("branch")
      .setFileExtension("nx3")
      .setTechnicalName("faf")
      .setAllowOverride(true)
      .setGitUrl("git@example.com/FAForever/faf"));

    Mockito.doAnswer(invocation -> {
      Path repoFolder = invocation.getArgument(0);
      Files.createDirectories(repoFolder.resolve("someDir"));
      Files.copy(
        LegacyFeaturedModDeploymentTaskTest.class.getResourceAsStream("/featured_mod/mod_info.lua"),
        repoFolder.resolve("mod_info.lua")
      );
      return null;
    }).when(gitWrapper).checkoutRef(any(), any());

    when(featuredModService.getFeaturedMods()).thenReturn(List.of(
      new FeaturedMod().setTechnicalName("faf")
    ));
    when(featuredModService.getFileIds("faf")).thenReturn(Map.of());

    Path dummyExe = repositoriesFolder.resolve("TemplateForgedAlliance.exe");
    createDummyExe(dummyExe);
    properties.getDeployment().setForgedAllianceExePath(dummyExe.toAbsolutePath().toString());

    instance.run();

    verify(featuredModService, never()).save(anyString(), anyShort(), any());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testRun() throws Exception {
    instance.setFeaturedMod(new FeaturedMod()
      .setGitBranch("branch")
      .setFileExtension("nx3")
      .setTechnicalName("faf")
      .setAllowOverride(true)
      .setGitUrl("git@example.com/FAForever/faf")
      .setDeploymentWebhook("someUrl"));

    Mockito.doAnswer(invocation -> {
      Path repoFolder = invocation.getArgument(0);
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

    when(featuredModService.getFeaturedMods()).thenReturn(List.of(
      new FeaturedMod().setTechnicalName("faf")
    ));
    when(featuredModService.getFileIds("faf")).thenReturn(Map.of(
      "ForgedAlliance.exe", 1,
      "someDir.nx3", 2
    ));

    Path dummyExe = repositoriesFolder.resolve("TemplateForgedAlliance.exe");
    createDummyExe(dummyExe);
    properties.getDeployment().setForgedAllianceExePath(dummyExe.toAbsolutePath().toString());

    instance.run();

    ArgumentCaptor<List<FeaturedModFile>> filesCaptor = ArgumentCaptor.forClass(List.class);
    verify(featuredModService).save(eq("faf"), eq((short) 1337), filesCaptor.capture());
    verify(restTemplate).getForObject("someUrl", String.class);

    List<FeaturedModFile> files = filesCaptor.getValue();

    assertThat(files, containsInAnyOrder(
      allOf(
        hasProperty("fileId", is((short) 1)),
        hasProperty("md5", is("47df959058cb52fe966ea5936dbd8f4c")),
        hasProperty("name", is("ForgedAlliance.1337.exe")),
        hasProperty("version", is(1337))
      ),
      allOf(
        hasProperty("fileId", is((short) 2)),
        hasProperty("md5", is(notNullValue())),
        hasProperty("name", is("someDir.1337.nx3")),
        hasProperty("version", is(1337))
      )
    ));

    assertThat(Files.exists(targetFolder.resolve("updates_faf_files/someDir.1337.nx3")), is(true));
    assertThat(Files.exists(targetFolder.resolve("updates_faf_files/ForgedAlliance.1337.exe")), is(true));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testHashEquality() throws Exception {
    instance.setFeaturedMod(new FeaturedMod()
      .setGitBranch("branch")
      .setFileExtension("nx3")
      .setTechnicalName("faf")
      .setAllowOverride(true)
      .setGitUrl("git@example.com/FAForever/faf")
      .setDeploymentWebhook("someUrl"));

    Mockito.doAnswer(invocation -> {
      Path repoFolder = invocation.getArgument(0);
      Files.createDirectories(repoFolder.resolve("someDir"));
      Files.copy(
        LegacyFeaturedModDeploymentTaskTest.class.getResourceAsStream("/featured_mod/mod_info.lua"),
        repoFolder.resolve("mod_info.lua"),
        StandardCopyOption.REPLACE_EXISTING
      );
      Files.copy(LegacyFeaturedModDeploymentTaskTest.class.getResourceAsStream("/featured_mod/someDir/someFile"),
        repoFolder.resolve("someDir/someFile"),
        StandardCopyOption.REPLACE_EXISTING
      );
      return null;
    }).when(gitWrapper).checkoutRef(any(), any());

    when(featuredModService.getFeaturedMods()).thenReturn(List.of(
      new FeaturedMod().setTechnicalName("faf")
    ));
    when(featuredModService.getFileIds("faf")).thenReturn(Map.of(
      "ForgedAlliance.exe", 1,
      "someDir.nx3", 2
    ));

    Path dummyExe = repositoriesFolder.resolve("TemplateForgedAlliance.exe");
    createDummyExe(dummyExe);
    properties.getDeployment().setForgedAllianceExePath(dummyExe.toAbsolutePath().toString());

    instance.run();

    ArgumentCaptor<List<FeaturedModFile>> filesCaptor = ArgumentCaptor.forClass(List.class);
    verify(featuredModService).save(eq("faf"), eq((short) 1337), filesCaptor.capture());

    List<FeaturedModFile> files1 = filesCaptor.getValue();

    instance.setFeaturedMod(new FeaturedMod()
      .setGitBranch("branch")
      .setFileExtension("nx3")
      .setTechnicalName("faf")
      .setAllowOverride(true)
      .setGitUrl("git@example.com/FAForever/faf")
      .setDeploymentWebhook("someUrl"));

    Mockito.doAnswer(invocation -> {
      Path repoFolder = invocation.getArgument(0);
      Files.createDirectories(repoFolder.resolve("someDir"));
      Files.copy(
        LegacyFeaturedModDeploymentTaskTest.class.getResourceAsStream("/featured_mod/mod_info.lua"),
        repoFolder.resolve("mod_info.lua"),
        StandardCopyOption.REPLACE_EXISTING
      );
      Files.copy(LegacyFeaturedModDeploymentTaskTest.class.getResourceAsStream("/featured_mod/someDir/someFile"),
        repoFolder.resolve("someDir/someFile"),
        StandardCopyOption.REPLACE_EXISTING
      );
      return null;
    }).when(gitWrapper).checkoutRef(any(), any());

    when(featuredModService.getFeaturedMods()).thenReturn(List.of(
      new FeaturedMod().setTechnicalName("faf")
    ));
    when(featuredModService.getFileIds("faf")).thenReturn(Map.of(
      "ForgedAlliance.exe", 1,
      "someDir.nx3", 2
    ));

    dummyExe = repositoriesFolder.resolve("TemplateForgedAlliance.exe");
    createDummyExe(dummyExe);
    properties.getDeployment().setForgedAllianceExePath(dummyExe.toAbsolutePath().toString());

    instance.run();

    filesCaptor = ArgumentCaptor.forClass(List.class);
    verify(featuredModService, times(2)).save(eq("faf"), eq((short) 1337), filesCaptor.capture());

    List<FeaturedModFile> files2 = filesCaptor.getValue();

    assertTrue(files1.stream().allMatch(file1 ->
      files2.stream().anyMatch(file2 ->
        Objects.equals(file1.getFileId(), file2.getFileId()) &&
          Objects.equals(file1.getMd5(), file2.getMd5()) &&
          Objects.equals(file1.getName(), file2.getName()) &&
          Objects.equals(file1.getVersion(), file2.getVersion())
      )));
  }

  @Test
  public void testInvokeDeploymentWebhookSkipped() {
    instance.invokeDeploymentWebhook(new FeaturedMod());
    verifyNoMoreInteractions(restTemplate);
  }

  @Test
  public void testInvokeDeploymentWebhookInvokedSuccess() {
    String someUrl = "someUrl";
    instance.invokeDeploymentWebhook(new FeaturedMod().setDeploymentWebhook(someUrl));

    verify(restTemplate).getForObject(someUrl, String.class);
    verifyNoMoreInteractions(restTemplate);
  }

  @Test
  public void testInvokeDeploymentWebhookResilience() {
    String someUrl = "someUrl";

    when(restTemplate.getForObject(anyString(), eq(String.class))).thenThrow(new RestClientException("error"));

    instance.invokeDeploymentWebhook(new FeaturedMod().setDeploymentWebhook(someUrl));

    verify(restTemplate).getForObject(someUrl, String.class);
    verifyNoMoreInteractions(restTemplate);
  }

  private void createDummyExe(Path file) throws IOException {
    try (RandomAccessFile randomAccessFile = new RandomAccessFile(file.toFile(), "rw")) {
      randomAccessFile.setLength(12_444_928);
    }
  }
}

package com.faforever.api.avatar;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.data.domain.Avatar;
import com.faforever.api.data.domain.GroupPermission;
import com.faforever.api.security.AuditService;
import com.faforever.api.security.OAuthScope;
import com.faforever.api.utils.FileHandlingHelper;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultUser.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepAvatarData.sql")
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sql/cleanAvatarData.sql")
public class AvatarControllerTest extends AbstractIntegrationTest {
  @SpyBean
  AuditService auditServiceSpy;

  @Autowired
  AvatarRepository avatarRepository;

  @Test
  public void canUploadWithScopeAndRole() throws Exception {
    mockMvc.perform(
      createAvatarUploadRequest()
        .with(getOAuthTokenWithTestUser(OAuthScope._UPLOAD_AVATAR, GroupPermission.ROLE_WRITE_AVATAR))
    ).andExpect(status().isCreated())
      .andExpect(content().string(""));
    final Avatar avatar = avatarRepository.findOneByUrl("http://localhost/faf/avatars/avatar3.png").get();
    assertThat(avatar.getUrl(), is("http://localhost/faf/avatars/avatar3.png"));
    assertThat(avatar.getTooltip(), is("Best avatar"));

    verify(auditServiceSpy, times(1)).logMessage(any());
  }

  @Test
  public void canReuploadWithScopeAndRole() throws Exception {
    Files.createDirectories(Paths.get("build/cache/avatars"));
    Files.copy(FileHandlingHelper.loadResourceAsStream("/avatars/donator.png"), Paths.get("build/cache/avatars/avatar1.png"));
    mockMvc.perform(
      createAvatarReuploadRequest(1)
        .with(getOAuthTokenWithTestUser(OAuthScope._UPLOAD_AVATAR, GroupPermission.ROLE_WRITE_AVATAR))
    ).andExpect(status().isOk())
      .andExpect(content().string(""));
    final Avatar avatar = avatarRepository.findOneByUrl("http://localhost/faf/avatars/avatar1.png").get();
    assertThat(avatar.getUrl(), is("http://localhost/faf/avatars/avatar1.png"));
    assertThat(avatar.getTooltip(), is("Best avatar"));
    verify(auditServiceSpy, times(1)).logMessage(any());
  }

  @Test
  public void canDeleteAvatarWithScopeAndRole() throws Exception {
    Files.createDirectories(Paths.get("build/cache/avatars"));
    Files.copy(FileHandlingHelper.loadResourceAsStream("/avatars/donator.png"), Paths.get("build/cache/avatars/avatar1.png"));
    mockMvc.perform(
      delete("/avatars/3")
        .with(getOAuthTokenWithTestUser(OAuthScope._UPLOAD_AVATAR, GroupPermission.ROLE_WRITE_AVATAR))
    ).andExpect(status().isNoContent());
    assertThat(avatarRepository.findById(3), is(Optional.empty()));
    verify(auditServiceSpy, times(1)).logMessage(any());
  }

  @Test
  public void cannotUploadWithoutRole() throws Exception {
    mockMvc.perform(
      createAvatarUploadRequest()
        .with(getOAuthTokenWithTestUser(OAuthScope._UPLOAD_AVATAR, NO_AUTHORITIES))
    ).andExpect(status().isForbidden());
    verify(auditServiceSpy, times(0)).logMessage(any());
  }

  @Test
  public void cannotReuploadWithoutRole() throws Exception {
    mockMvc.perform(
      createAvatarReuploadRequest(1)
        .with(getOAuthTokenWithTestUser(OAuthScope._UPLOAD_AVATAR, NO_AUTHORITIES))
    ).andExpect(status().isForbidden());
    verify(auditServiceSpy, times(0)).logMessage(any());
  }

  @Test
  public void cannotDeleteWithoutRole() throws Exception {
    mockMvc.perform(
      delete("/avatars/1")
        .with(getOAuthTokenWithTestUser(OAuthScope._UPLOAD_AVATAR, NO_AUTHORITIES))
    ).andExpect(status().isForbidden());
    verify(auditServiceSpy, times(0)).logMessage(any());
  }

  @Test
  public void cannotUploadWithoutScope() throws Exception {
    mockMvc.perform(
      createAvatarUploadRequest()
        .with(getOAuthTokenWithTestUser(NO_SCOPE, GroupPermission.ROLE_WRITE_AVATAR))
    ).andExpect(status().isForbidden());
    verify(auditServiceSpy, times(0)).logMessage(any());
  }

  @Test
  public void cannotReuploadWithoutScope() throws Exception {
    mockMvc.perform(
      createAvatarReuploadRequest(1)
        .with(getOAuthTokenWithTestUser(NO_SCOPE, GroupPermission.ROLE_WRITE_AVATAR))
    ).andExpect(status().isForbidden());
    verify(auditServiceSpy, times(0)).logMessage(any());
  }

  @Test
  public void cannotDeleteWithoutScope() throws Exception {
    mockMvc.perform(
      delete("/avatars/1")
        .with(getOAuthTokenWithTestUser(NO_SCOPE, GroupPermission.ROLE_WRITE_AVATAR))
    ).andExpect(status().isForbidden());
    verify(auditServiceSpy, times(0)).logMessage(any());
  }

  @After
  public void tearDown() throws Exception {
    Files.deleteIfExists(Paths.get("build/cache/avatars/avatar1.png"));
    Files.deleteIfExists(Paths.get("build/cache/avatars/avatar3.png"));
  }

  private MockMultipartHttpServletRequestBuilder createAvatarUploadRequest() throws IOException {
    return MockMvcRequestBuilders.fileUpload("/avatars/upload")
      .file(new MockMultipartFile("file", "avatar3.png", MediaType.IMAGE_PNG_VALUE, FileHandlingHelper.loadResourceAsStream("/avatars/donator.png")))
      .file(new MockMultipartFile("metadata", "metadata.json", MediaType.APPLICATION_JSON_VALUE, FileHandlingHelper.loadResourceAsStream("/avatars/metadata.json")));
  }

  private MockMultipartHttpServletRequestBuilder createAvatarReuploadRequest(Integer id) throws IOException {
    return MockMvcRequestBuilders.fileUpload("/avatars/{id}/upload", id)
      .file(new MockMultipartFile("file", "avatar1.png", MediaType.IMAGE_PNG_VALUE, FileHandlingHelper.loadResourceAsStream("/avatars/donator.png")))
      .file(new MockMultipartFile("metadata", "metadata.json", MediaType.APPLICATION_JSON_VALUE, FileHandlingHelper.loadResourceAsStream("/avatars/metadata.json")));
  }
}

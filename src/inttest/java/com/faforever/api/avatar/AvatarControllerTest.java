package com.faforever.api.avatar;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.data.domain.Avatar;
import com.faforever.api.security.OAuthScope;
import com.faforever.api.utils.FileHandlingHelper;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultUser.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepAvatarData.sql")
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sql/cleanAvatarData.sql")
public class AvatarControllerTest extends AbstractIntegrationTest {

  @Autowired
  AvatarRepository avatarRepository;

  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void moderatorCanUpload() throws Exception {
    mockMvc.perform(
      createAvatarUploadRequest()
        .with(getOAuthToken(OAuthScope._UPLOAD_AVATAR))
    ).andExpect(status().isCreated())
      .andExpect(content().string(""));
    final Avatar avatar = avatarRepository.findOneByUrl("http://localhost/faf/avatars/donator.png").get();
    assertThat(avatar.getUrl(), is("http://localhost/faf/avatars/donator.png"));
    assertThat(avatar.getTooltip(), is("Best avatar"));
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void nonModeratorCannotUpload() throws Exception {
    mockMvc.perform(
      createAvatarUploadRequest()
        .with(getOAuthToken(OAuthScope._UPLOAD_AVATAR))
    ).andExpect(status().isForbidden());
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void moderatorWithoutScopeCannotUpload() throws Exception {
    mockMvc.perform(
      createAvatarUploadRequest()
        .with(getOAuthToken())
    ).andExpect(status().isForbidden());
  }

  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void moderatorCanDeleteAvatar() throws Exception {
    Files.copy(FileHandlingHelper.loadResourceAsStream("/avatars/donator.png"), Paths.get("build/cache/avatars/avatar1.png"));
    mockMvc.perform(
      delete("/avatars/1")
        .with(getOAuthToken(OAuthScope._UPLOAD_AVATAR))
    ).andExpect(status().isNoContent());
    assertThat(avatarRepository.findById(1), is(Optional.empty()));
  }

  @After
  public void tearDown() throws Exception {
    Files.deleteIfExists(Paths.get("build/cache/avatars/donator.png"));
  }

  private MockMultipartHttpServletRequestBuilder createAvatarUploadRequest() throws IOException {
    return MockMvcRequestBuilders.fileUpload("/avatars/upload")
      .file(new MockMultipartFile("file", "donator.png", MediaType.IMAGE_PNG_VALUE, FileHandlingHelper.loadResourceAsStream("/avatars/donator.png")))
      .file(new MockMultipartFile("metadata", "metadata.json", MediaType.APPLICATION_JSON_VALUE, FileHandlingHelper.loadResourceAsStream("/avatars/metadata.json")));
  }
}

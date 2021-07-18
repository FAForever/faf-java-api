package com.faforever.api.mod;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.config.FafApiProperties;
import com.faforever.api.security.OAuthScope;
import com.google.common.io.ByteStreams;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;

import java.io.InputStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ModsControllerTest extends AbstractIntegrationTest {

  @Autowired
  private FafApiProperties apiProperties;

  @WithUserDetails(AUTH_USER)
  @Test
  void missingScope() throws Exception {
    String zipFile = "Terrain_Deform_for_FA.v0001.zip";
    try (InputStream inputStream = loadModResourceAsStream(zipFile)) {
      MockMultipartFile file = new MockMultipartFile("file",
        zipFile,
        "application/zip",
        ByteStreams.toByteArray(inputStream));

      mockMvc.perform(multipart("/mods/upload")
        .file(file)
        .with(getOAuthTokenWithTestUser(NO_SCOPE, NO_AUTHORITIES)))
        .andExpect(status().isForbidden());
    } finally {
      FileUtils.deleteDirectory(apiProperties.getMod().getTargetDirectory().toFile());
    }
  }

  @WithUserDetails(AUTH_USER)
  @Test
  void successUpload() throws Exception {
    String zipFile = "Terrain_Deform_for_FA.v0001.zip";
    try (InputStream inputStream = loadModResourceAsStream(zipFile)) {
      MockMultipartFile file = new MockMultipartFile("file",
        zipFile,
        "application/zip",
        ByteStreams.toByteArray(inputStream));

      mockMvc.perform(multipart("/mods/upload")
        .file(file)
        .with(getOAuthTokenWithTestUser(OAuthScope._UPLOAD_MOD, NO_AUTHORITIES))
      ).andExpect(status().isOk());
    } finally {
      FileUtils.deleteDirectory(apiProperties.getMod().getTargetDirectory().toFile());
    }
  }

  private InputStream loadModResourceAsStream(String filename) {
    return ModsControllerTest.class.getResourceAsStream("/mods/" + filename);
  }

}

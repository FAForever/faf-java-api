package com.faforever.api.map;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.security.OAuthScope;
import com.google.common.io.ByteStreams;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;

import java.io.InputStream;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/truncateTables.sql")
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultData.sql")
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepMapVersion.sql")
public class MapsControllerTest extends AbstractIntegrationTest{

  @Autowired
  MapRepository mapRepository;

  @WithUserDetails(AUTH_USER)
  @Test
  void missingScope() throws Exception {
    mockMvc.perform(multipart("/maps/upload")
      .with(getOAuthTokenWithTestUser(NO_SCOPE, NO_AUTHORITIES)))
      .andExpect(status().isBadRequest());
  }

  @WithUserDetails(AUTH_USER)
  @Test
  void fileMissing() throws Exception {
    MockMultipartFile file = new MockMultipartFile("file", "filename.txt", "text/plain", "some xml".getBytes());

    mockMvc.perform(multipart("/maps/upload")
      .file(file)
      .with(getOAuthTokenWithTestUser(OAuthScope._UPLOAD_MAP, NO_AUTHORITIES)))
      .andExpect(status().isForbidden())
      .andExpect(jsonPath("$.errors", hasSize(1)))
      .andExpect(jsonPath("$.errors[0].title", is("org.springframework.web.multipart.support.MissingServletRequestPartException")))
      .andExpect(jsonPath("$.errors[0].detail", is("Required request part 'file' is not present")));
  }

  @WithUserDetails(AUTH_USER)
  @Test
  void jsonMetaDataMissing() throws Exception {
    MockMultipartFile file = new MockMultipartFile("file", "filename.txt", "text/plain", "some xml".getBytes());

    mockMvc.perform(multipart("/maps/upload")
      .file(file)
      .with(getOAuthTokenWithTestUser(OAuthScope._UPLOAD_MAP, NO_AUTHORITIES)))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors", hasSize(1)))
      .andExpect(jsonPath("$.errors[0].title", is("org.springframework.web.bind.MissingServletRequestParameterException")))
      .andExpect(jsonPath("$.errors[0].detail", is("Required request parameter 'metadata' for method parameter type String is not present")));
  }

  @WithUserDetails(AUTH_USER)
  @Test
  void successUpload() throws Exception {
    String jsonString = "{}";

    String zipFile = "command_conquer_rush.v0007.zip";
    try (InputStream inputStream = loadMapResourceAsStream(zipFile)) {
      MockMultipartFile file = new MockMultipartFile("file",
        zipFile,
        "application/zip",
        ByteStreams.toByteArray(inputStream));

      mockMvc.perform(multipart("/maps/upload")
        .file(file)
        .with(getOAuthTokenWithTestUser(OAuthScope._UPLOAD_MAP, NO_AUTHORITIES))
        .param("metadata", jsonString)
      ).andExpect(status().isOk());
    }
  }

  private InputStream loadMapResourceAsStream(String filename) {
    return MapsControllerTest.class.getResourceAsStream("/maps/" + filename);
  }
}

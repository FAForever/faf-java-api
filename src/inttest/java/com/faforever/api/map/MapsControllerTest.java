package com.faforever.api.map;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.config.FafApiProperties;
import com.faforever.api.security.OAuthScope;
import com.google.common.io.ByteStreams;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
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
  private MapRepository mapRepository;

  @Autowired
  private FafApiProperties fafApiProperties;

  @Test
  void missingScope() throws Exception {
    String jsonString = "{}";
    MockMultipartFile file = new MockMultipartFile("file", "filename.txt", "text/plain", "some xml".getBytes());

    mockMvc.perform(multipart("/maps/upload")
      .file(file)
      .with(getOAuthTokenWithActiveUser(NO_SCOPE, NO_AUTHORITIES))
      .param("metadata", jsonString))
      .andExpect(status().isForbidden());
  }

  @Test
  void fileMissing() throws Exception {
    mockMvc.perform(multipart("/maps/upload")
        .with(getOAuthTokenWithActiveUser(OAuthScope._UPLOAD_MAP, NO_AUTHORITIES)))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors", hasSize(1)))
      .andExpect(jsonPath("$.errors[0].title", is("org.springframework.web.multipart.support.MissingServletRequestPartException")))
      .andExpect(jsonPath("$.errors[0].detail", is("Required part 'file' is not present.")));
  }

  @Test
  void jsonMetaDataMissing() throws Exception {
    MockMultipartFile file = new MockMultipartFile("file", "filename.txt", "text/plain", "some xml".getBytes());

    mockMvc.perform(multipart("/maps/upload")
      .file(file)
      .with(getOAuthTokenWithActiveUser(OAuthScope._UPLOAD_MAP, NO_AUTHORITIES)))
      .andExpect(status().isUnprocessableEntity())
      .andExpect(jsonPath("$.errors", hasSize(1)))
      .andExpect(jsonPath("$.errors[0].title", is("Missing parameter")))
      .andExpect(jsonPath("$.errors[0].detail", is("A parameter 'metadata' has to be provided.")));
  }

  @Test
  void successUploadViaJsonRequestParam() throws Exception {
    String jsonString = """
      {
        "isRanked": true
      }
      """;

    String zipFile = "command_conquer_rush.v0007.zip";
    try (InputStream inputStream = loadMapResourceAsStream(zipFile)) {
      MockMultipartFile file = new MockMultipartFile("file",
        zipFile,
        "application/zip",
        ByteStreams.toByteArray(inputStream));

      mockMvc.perform(multipart("/maps/upload")
        .file(file)
        .with(getOAuthTokenWithActiveUser(OAuthScope._UPLOAD_MAP, NO_AUTHORITIES))
        .param("metadata", jsonString)
      ).andExpect(status().isOk());
    } finally {
      FileUtils.deleteDirectory(fafApiProperties.getMap().getTargetDirectory().toFile());
    }
  }

  @Test
  void successUploadViaMultipartJsonMetadata() throws Exception {
    String metadataString = """
      {
        "isRanked": true,
        "licenseId": 1
      }
      """;
    MockMultipartFile metadata = new MockMultipartFile("metadata", null, "application/json", metadataString.getBytes());

    String zipFile = "command_conquer_rush.v0007.zip";
    try (InputStream inputStream = loadMapResourceAsStream(zipFile)) {
      MockMultipartFile file = new MockMultipartFile("file",
        zipFile,
        "application/zip",
        ByteStreams.toByteArray(inputStream));

      mockMvc.perform(multipart("/maps/upload")
        .file(file)
        .file(metadata)
        .with(getOAuthTokenWithActiveUser(OAuthScope._UPLOAD_MAP, NO_AUTHORITIES))
      ).andExpect(status().isOk());
    } finally {
      FileUtils.deleteDirectory(fafApiProperties.getMap().getTargetDirectory().toFile());
    }
  }

  private InputStream loadMapResourceAsStream(String filename) {
    return MapsControllerTest.class.getResourceAsStream("/maps/" + filename);
  }
}

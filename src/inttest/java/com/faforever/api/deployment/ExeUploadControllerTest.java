package com.faforever.api.deployment;

import com.faforever.api.AbstractIntegrationTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepFeaturedMods.sql")
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sql/cleanFeaturedMods.sql")
public class ExeUploadControllerTest extends AbstractIntegrationTest {
  private MockMultipartFile file;
  private static final String SUPER_SECRET = "banana";

  @Before
  public void setUp() {
    super.setUp();
    file = new MockMultipartFile("file", "ForgedAlliance.exe", "application/octet-stream", new byte[]{ 1, 2 ,3, 4 });
  }

  @Test
  public void testSuccessUploadBeta() throws Exception {
    this.mockMvc.perform(fileUpload("/exe/upload")
      .file(file)
      .param("modName", "fafbeta")
      .param("apiKey", SUPER_SECRET)
    ).andExpect(status().isOk());
    assertTrue(Files.exists(Paths.get("build/exe/beta/ForgedAlliance.3706.exe")));
  }

  @Test
  public void testSuccessUploadDevelop() throws Exception {
    this.mockMvc.perform(fileUpload("/exe/upload")
      .file(file)
      .param("modName", "fafdevelop")
      .param("apiKey", SUPER_SECRET)
    ).andExpect(status().isOk());
    assertTrue(Files.exists(Paths.get("build/exe/develop/ForgedAlliance.3707.exe")));
  }

  @Test
  public void testBadRequestUploadNoModName() throws Exception  {
    this.mockMvc.perform(fileUpload("/exe/upload")
      .file(file)
      .param("apiKey", SUPER_SECRET)
    ).andExpect(status().is4xxClientError());
  }

  @Test
  public void testBadRequestUploadNoFile() throws Exception {
    this.mockMvc.perform(fileUpload("/exe/upload")
      .param("modName", "fafdevelop")
      .param("apiKey", SUPER_SECRET)
    ).andExpect(status().is4xxClientError());
  }

  @Test
  public void testBadRequestUploadFileWithWrongExeExtension() throws Exception  {
    MockMultipartFile file = new MockMultipartFile("file", "ForgedAlliance.zip", "application/octet-stream", new byte[]{ 1, 2 ,3, 4 });
    this.mockMvc.perform(fileUpload("/exe/upload")
      .file(file)
      .param("modName", "fafbeta")
      .param("apiKey", SUPER_SECRET)
    ).andExpect(status().is4xxClientError());
  }

  @Test
  public void testBadRequestUploadWithoutApiKey() throws Exception  {
    this.mockMvc.perform(fileUpload("/exe/upload")
      .file(file)
      .param("modName", "fafbeta")
    ).andExpect(status().is4xxClientError());
  }

  @Test
  public void testBadRequestUploadWithWrongApiKey() throws Exception  {
    this.mockMvc.perform(fileUpload("/exe/upload")
      .file(file)
      .param("modName", "fafbeta")
      .param("apiKey", "not a banana")
    ).andExpect(status().is4xxClientError());
  }
}

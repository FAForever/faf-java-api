package com.faforever.api.deployment;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.config.TestWebSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import javax.inject.Inject;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ExeUploaderController.class)
@Import(TestWebSecurityConfig.class)
public class ExeUploaderControllerTest {

  public static final String API_KEY = "banana";
  private MockMvc mvc;
  @MockBean
  private ExeUploaderService exeUploaderService;
  @MockBean
  private FafApiProperties fafApiProperties;
  @MockBean
  private FafApiProperties.Deployment deployment;

  @Mock
  private MockMultipartFile file;

  @Inject
  public void init(MockMvc mvc) {
    this.mvc = mvc;
  }

  @BeforeEach
  public void setUp() {
    when(fafApiProperties.getDeployment()).thenReturn(deployment);
    when(deployment.getAllowedExeExtension()).thenReturn("exe");
    when(deployment.getTestingExeUploadKey()).thenReturn(API_KEY);
    file = new MockMultipartFile("file",
      "ForgedAlliance.exe",
      "application/octet-stream",
      new byte[]{1, 2, 3, 4});
  }

  @Test
  public void testSuccessUpload() throws Exception {
    this.mvc.perform(fileUpload("/exe/upload")
      .file(file)
      .param("modName", "fafbeta")
      .param("apiKey", API_KEY)
    ).andExpect(status().isOk());
  }

  @Test
  public void testBadRequestUploadNoModName() throws Exception {
    this.mvc.perform(fileUpload("/exe/upload")
      .file(file)
      .param("apiKey", API_KEY)
    ).andExpect(status().isBadRequest());
  }

  @Test
  public void testBadRequestUploadNoFile() throws Exception {
    this.mvc.perform(fileUpload("/exe/upload")
      .param("modName", "fafbeta2222")
      .param("apiKey", API_KEY)
    ).andExpect(status().isBadRequest());
  }

  @Test
  public void testBadRequestUploadFileWithWrongExeExtension() throws Exception {
    MockMultipartFile file = new MockMultipartFile("file",
      "ForgedAlliance.zip",
      "application/octet-stream",
      new byte[]{1, 2, 3, 4});
    this.mvc.perform(fileUpload("/exe/upload")
      .file(file)
      .param("modName", "fafbeta")
      .param("apiKey", API_KEY)
    ).andExpect(status().is4xxClientError());
  }

  @Test
  public void testBadRequestUploadWithoutApiKey() throws Exception {
    this.mvc.perform(fileUpload("/exe/upload")
      .file(file)
      .param("modName", "fafbeta")
    ).andExpect(status().isBadRequest());
  }
}

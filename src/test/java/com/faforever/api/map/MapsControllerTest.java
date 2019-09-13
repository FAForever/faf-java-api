package com.faforever.api.map;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.config.TestWebSecurityConfig;
import com.faforever.api.player.PlayerService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import javax.inject.Inject;
import java.io.InputStream;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(MapsController.class)
@Import(TestWebSecurityConfig.class)
public class MapsControllerTest {

  private MockMvc mvc;
  @MockBean
  private MapService mapService;
  @MockBean
  private FafApiProperties fafApiProperties;
  @MockBean
  private PlayerService playerService;
  @MockBean
  private ObjectMapper objectMapper;

  @Inject
  void init(MockMvc mvc) {
    this.mvc = mvc;
  }

  @Test
  void fileMissing() throws Exception {
    this.mvc.perform(multipart("/maps/upload"))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors", hasSize(1)))
      .andExpect(jsonPath("$.errors[0].status", is(HttpStatus.BAD_REQUEST.toString())))
      .andExpect(jsonPath("$.errors[0].title", is("org.springframework.web.multipart.support.MissingServletRequestPartException")))
      .andExpect(jsonPath("$.errors[0].detail", is("Required request part 'file' is not present")));
  }

  @Test
  void jsonMetaDataMissing() throws Exception {
    MockMultipartFile file = new MockMultipartFile("file", "filename.txt", "text/plain", "some xml".getBytes());

    this.mvc.perform(multipart("/maps/upload")
      .file(file))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors", hasSize(1)))
      .andExpect(jsonPath("$.errors[0].status", is(HttpStatus.BAD_REQUEST.toString())))
      .andExpect(jsonPath("$.errors[0].title", is("org.springframework.web.bind.MissingServletRequestParameterException")))
      .andExpect(jsonPath("$.errors[0].detail", is("Required String parameter 'metadata' is not present")));
  }

  @Test
  void successUpload() throws Exception {
    FafApiProperties props = new FafApiProperties();
    String jsonString = "{}";
    ObjectMapper mapper = new ObjectMapper();
    JsonNode node = mapper.readTree(jsonString);

    when(fafApiProperties.getMap()).thenReturn(props.getMap());
    when(objectMapper.readTree(anyString())).thenReturn(node);
    String zipFile = "command_conquer_rush.v0007.zip";
    try (InputStream inputStream = loadMapResourceAsStream(zipFile)) {
      MockMultipartFile file = new MockMultipartFile("file",
        zipFile,
        "application/zip",
        ByteStreams.toByteArray(inputStream));

      this.mvc.perform(multipart("/maps/upload")
        .file(file)
        .param("metadata", jsonString)
      ).andExpect(status().isOk());
    }
  }

  private InputStream loadMapResourceAsStream(String filename) {
    return MapsControllerTest.class.getResourceAsStream("/maps/" + filename);
  }
}

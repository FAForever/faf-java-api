package com.faforever.api.map;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.config.TestWebSecurityConfig;
import com.faforever.api.player.PlayerRepository;
import com.faforever.api.player.PlayerService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.InputStream;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(MapsController.class)
@Import(TestWebSecurityConfig.class)
public class MapControllerTest {

  @Autowired
  private MockMvc mvc;
  @MockBean
  private PlayerRepository playerRepository;
  @MockBean
  private MapRepository mapRepository;
  @MockBean
  private MapService mapService;
  @MockBean
  private FafApiProperties fafApiProperties;
  @MockBean
  private PlayerService playerService;
  @MockBean
  private ObjectMapper objectMapper;


  @Test
  public void fileMissing() throws Exception {
    this.mvc.perform(fileUpload("/maps/upload"))
        .andExpect(status().is(400))
        .andExpect(status().reason("Required request part 'file' is not present"));

  }

  @Test
  public void jsonMetaDataMissing() throws Exception {
    MockMultipartFile file = new MockMultipartFile("file", "filename.txt", "text/plain", "some xml".getBytes());

    this.mvc.perform(fileUpload("/maps/upload")
        .file(file))
        .andExpect(status().is(400))
        .andExpect(status().reason("Required String parameter 'metadata' is not present"));
  }

  @Test
  public void successUpload() throws Exception {
    FafApiProperties props = new FafApiProperties();
    String jsonString = "{}";
    ObjectMapper mapper = new ObjectMapper();
    JsonNode node = mapper.readTree(jsonString);

    when(fafApiProperties.getMap()).thenReturn(props.getMap());
    when(objectMapper.readTree(anyString())).thenReturn(node);
    String zipFile = "scmp_037.zip";
    try (InputStream inputStream = loadMapResourceAsStream(zipFile)) {
      MockMultipartFile file = new MockMultipartFile("file",
          zipFile,
          "application/zip",
          ByteStreams.toByteArray(inputStream));

      this.mvc.perform(fileUpload("/maps/upload")
          .file(file)
          .param("metadata", jsonString)
      ).andExpect(status().isOk());
    }
  }

  private InputStream loadMapResourceAsStream(String filename) {
    return MapControllerTest.class.getResourceAsStream("/maps/" + filename);
  }
}

package com.faforever.api.map;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.config.TestWebSecurityConfig;
import com.faforever.api.player.PlayerRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(MapsController.class)
@ImportAutoConfiguration(TestWebSecurityConfig.class)
// TODO: test_maps_upload_is_metadata_missing
// TODO: test_maps_upload_no_file_results_400
// TODO: test_maps_upload_txt_results_400
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


  @Test
  public void fileMissing() throws Exception {
    this.mvc.perform(fileUpload("/maps/upload"))
        .andExpect(status().is(400))
        .andExpect(status().reason("Required request part 'file' is not present"));

  }

  @Test
  public void jsonMetaDataMissing() throws Exception {
    MockMultipartFile firstFile = new MockMultipartFile("file", "filename.txt", "text/plain", "some xml".getBytes());

    this.mvc.perform(fileUpload("/maps/upload")
        .file(firstFile))
        .andExpect(status().is(400))
        .andExpect(status().reason("Required String parameter 'metadata' is not present"));
  }
}

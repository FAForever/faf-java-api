package com.faforever.api.map;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.player.PlayerRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(MapsController.class)
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
  public void jsonMetaDataMissing() throws Exception {
    // TODO: we need to login
    this.mvc.perform(post("/maps/upload")).andExpect(status().isOk());
  }
}

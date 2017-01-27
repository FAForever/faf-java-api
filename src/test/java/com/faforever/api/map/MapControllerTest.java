//package com.faforever.api.map;
//
//import com.faforever.api.FafApiApplication;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.boot.test.SpringApplicationConfiguration;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import org.springframework.test.context.web.WebAppConfiguration;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.web.context.WebApplicationContext;
//
//import javax.inject.Inject;
//
//import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.fileUpload;
//import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
//
//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(classes = FafApiApplication.class)
//@WebAppConfiguration
//// TODO: test_maps_upload_is_metadata_missing
//// TODO: test_maps_upload_no_file_results_400
//// TODO: test_maps_upload_txt_results_400
//public class MapControllerTest {
//
//  private MockMvc mockMvc;
//
//  private WebApplicationContext webApplicationContext;
//
//  @Inject
//  public void Init(WebApplicationContext webApplicationContext) {
//    this.webApplicationContext = webApplicationContext;
//  }
//
//  @Before
//  public void setup() throws Exception {
//    this.mockMvc = webAppContextSetup(webApplicationContext).build();
//  }
//
//  @Test
//  public void jsonMetaDataMissing() throws Exception {
//    MockMultipartFile myMap = new MockMultipartFile("file", "filename.txt", "text/plain", "some xml".getBytes());
//    MockMultipartFile myJson = new MockMultipartFile("json", "", "application/json", "{\"json\": \"someValue\"}".getBytes());
//
//    mockMvc.perform(fileUpload("/maps/upload")
//        .file(myMap)
//        .file(myJson));
//  }
//}

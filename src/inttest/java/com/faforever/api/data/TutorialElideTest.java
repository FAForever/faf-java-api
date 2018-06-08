package com.faforever.api.data;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.data.domain.Tutorial;
import com.faforever.api.data.domain.TutorialCategory;
import com.github.jasminb.jsonapi.JSONAPIDocument;
import com.github.jasminb.jsonapi.ResourceConverter;
import com.jayway.jsonpath.JsonPath;
import org.junit.Test;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultUser.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepTutorialData.sql")
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sql/cleanTutorialData.sql")
public class TutorialElideTest extends AbstractIntegrationTest {
  private static Tutorial tutorial() {
    Tutorial tutorial = new Tutorial();
    TutorialCategory category = new TutorialCategory();
    category.setId(1);
    tutorial.setCategory(category);
    tutorial.setImage("abc.png");
    tutorial.setDescription("abc");
    tutorial.setLaunchable(true);
    tutorial.setTechnicalName("tec_name");
    tutorial.setTitleKey("abc");
    tutorial.setOrdinal(0);
    return tutorial;
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void nonEmptyResultTutorialsAsUser() throws Exception {
    mockMvc.perform(get("/data/tutorial"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(1)));
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void canReadSpecificTutorialAsUser() throws Exception {
    mockMvc.perform(get("/data/tutorial/1"))
      .andExpect(status().isOk());
  }

  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void canReadTutorialsAsModerator() throws Exception {
    mockMvc.perform(get("/data/tutorial"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(1)));
  }

  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void canReadSpecificTutorialAsModerator() throws Exception {
    mockMvc.perform(get("/data/tutorial/1"))
      .andExpect(status().isOk());
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void cannotPostTutorialAsUser() throws Exception {
    final ResourceConverter resourceConverter = new ResourceConverter(objectMapper, Tutorial.class);

    mockMvc.perform(post("/data/tutorial")
      .content(resourceConverter.writeDocument(new JSONAPIDocument<>(tutorial()))))
      .andExpect(status().isForbidden());
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void cannotDeleteTutorialAsUser() throws Exception {
    mockMvc.perform(delete("/data/tutorial/1"))
      .andExpect(status().isForbidden());
  }


  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void moderatorCanPostTutorial() throws Exception {
    final ResourceConverter resourceConverter = new ResourceConverter(objectMapper, Tutorial.class);

    MvcResult mvcResult = mockMvc.perform(post("/data/tutorial").content(resourceConverter.writeDocument(new JSONAPIDocument<>(tutorial()))))
      .andExpect(status().isCreated())
      .andReturn();

    String id = JsonPath.parse(mvcResult.getResponse().getContentAsString()).read("$.data.id");
    mockMvc.perform(get("/data/tutorial/{0}", id))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.id", equalTo(id)));
  }
}

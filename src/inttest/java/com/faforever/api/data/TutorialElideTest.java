package com.faforever.api.data;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.data.domain.GroupPermission;
import com.faforever.api.security.OAuthScope;
import com.faforever.commons.api.dto.Tutorial;
import com.faforever.commons.api.dto.TutorialCategory;
import com.jayway.jsonpath.JsonPath;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
    category.setId("1");
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
  public void nonEmptyResultTutorialsWithoutScopeAndRole() throws Exception {
    mockMvc.perform(get("/data/tutorial")
      .with(getOAuthTokenWithTestUser(NO_SCOPE, NO_AUTHORITIES)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(1)));
  }

  @Test
  public void canReadSpecificTutorialWithoutScopeAndRole() throws Exception {
    mockMvc.perform(get("/data/tutorial/1")
      .with(getOAuthTokenWithTestUser(NO_SCOPE, NO_AUTHORITIES)))
      .andExpect(status().isOk());
  }


  @Test
  public void canDeleteTutorialWithScopeAndRole() throws Exception {
    mockMvc.perform(delete("/data/tutorial/1")
      .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_WRITE_TUTORIAL)))
      .andExpect(status().isNoContent());
  }

  @Test
  public void cannotDeleteTutorialWithoutScope() throws Exception {
    mockMvc.perform(delete("/data/tutorial/1")
      .with(getOAuthTokenWithTestUser(NO_SCOPE, GroupPermission.ROLE_WRITE_TUTORIAL)))
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotDeleteTutorialWithoutRole() throws Exception {
    mockMvc.perform(delete("/data/tutorial/1")
      .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, NO_AUTHORITIES)))
      .andExpect(status().isForbidden());
  }

  @Test
  public void canPostTutorialWithScopeAndRole() throws Exception {
    MvcResult mvcResult = mockMvc.perform(
      post("/data/tutorial")
        .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_WRITE_TUTORIAL))
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(tutorial())))
      .andExpect(status().isCreated())
      .andReturn();

    String id = JsonPath.parse(mvcResult.getResponse().getContentAsString()).read("$.data.id");
    mockMvc.perform(get("/data/tutorial/{0}", id)
      .with(getOAuthTokenWithTestUser(NO_SCOPE, NO_AUTHORITIES)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.id", equalTo(id)));
  }

  @Test
  public void cannotPostTutorialWithoutScope() throws Exception {
    mockMvc.perform(
      post("/data/tutorial")
        .with(getOAuthTokenWithTestUser(NO_SCOPE, GroupPermission.ROLE_WRITE_TUTORIAL))
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(tutorial())))
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotPostTutorialWithoutRole() throws Exception {
    mockMvc.perform(
      post("/data/tutorial")
        .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(tutorial())))
      .andExpect(status().isForbidden());
  }

  @Test
  public void canUpdateTutorialWithScopeAndRole() throws Exception {
    mockMvc.perform(
      patch("/data/tutorial/1")
        .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_WRITE_TUTORIAL))
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(tutorial().setDescription("changed").setId("1"))))
      .andExpect(status().isNoContent());

    mockMvc.perform(get("/data/tutorial/1")
      .with(getOAuthTokenWithTestUser(NO_SCOPE, NO_AUTHORITIES)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.attributes.description", is("changed")));
  }

  @Test
  public void cannotUpdateTutorialWithoutScope() throws Exception {
    mockMvc.perform(
      patch("/data/tutorial/1")
        .with(getOAuthTokenWithTestUser(NO_SCOPE, GroupPermission.ROLE_WRITE_TUTORIAL))
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(tutorial().setDescription("changed").setId("1"))))
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotUpdateTutorialWithoutRole() throws Exception {
    mockMvc.perform(
      patch("/data/tutorial/1")
        .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(createJsonApiContent(tutorial().setDescription("changed").setId("1"))))
      .andExpect(status().isForbidden());
  }
}

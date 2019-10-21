package com.faforever.api.data;

import com.faforever.api.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultUser.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDomainBlacklistData.sql")
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sql/cleanDomainBlacklistData.sql")
public class DomainBlacklistTest extends AbstractIntegrationTest {
  private static final String NEW_DOMAIN = "{\"data\":{\"type\":\"domainBlacklist\",\"id\":\"google.com\"}}";

  @Test
  @WithUserDetails(AUTH_USER)
  public void emptyResultDomainBlacklistAsUser() throws Exception {
    mockMvc.perform(get("/data/domainBlacklist"))
      .andExpect(status().isOk())
      .andExpect(content().string("{\"data\":[]}"));
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void cannotReadSpecificDomainBlacklistAsUser() throws Exception {
    mockMvc.perform(get("/data/domainBlacklist/spam.org"))
      .andExpect(status().isForbidden());
  }

  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void canReadDomainBlacklistAsModerator() throws Exception {
    mockMvc.perform(get("/data/domainBlacklist"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(1)));
  }

  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void canReadSpecificDomainBlacklistAsModerator() throws Exception {
    mockMvc.perform(get("/data/domainBlacklist/spam.org"))
      .andExpect(status().isOk());
  }


  @Test
  @WithUserDetails(AUTH_USER)
  public void cannotCreateDomainBlacklistAsUser() throws Exception {
    mockMvc.perform(
      post("/data/domainBlacklist")
        .header(HttpHeaders.CONTENT_TYPE, JsonApiMediaType.JSON_API_MEDIA_TYPE)
        .content(NEW_DOMAIN))
      .andExpect(status().isForbidden());
  }

  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void canCreateDomainBlacklistAsModerator() throws Exception {
    mockMvc.perform(
      post("/data/domainBlacklist")
        .header(HttpHeaders.CONTENT_TYPE, JsonApiMediaType.JSON_API_MEDIA_TYPE)
        .content(NEW_DOMAIN))
      .andExpect(status().isCreated());
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void cannotDeleteDomainBlacklistAsUser() throws Exception {
    mockMvc.perform(
      delete("/data/domainBlacklist/spam.org"))
      .andExpect(status().isForbidden());
  }

  @Test
  @WithUserDetails(AUTH_MODERATOR)
  public void canDeleteDomainBlacklistAsModerator() throws Exception {
    mockMvc.perform(
      delete("/data/domainBlacklist/spam.org"))
      .andExpect(status().isNoContent());
  }
}

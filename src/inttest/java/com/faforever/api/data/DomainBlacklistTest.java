package com.faforever.api.data;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.data.domain.GroupPermission;
import com.faforever.api.security.OAuthScope;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultUser.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDomainBlacklistData.sql")
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sql/cleanDomainBlacklistData.sql")
public class DomainBlacklistTest extends AbstractIntegrationTest {
  private static final String NEW_DOMAIN = "{\"data\":{\"type\":\"domainBlacklist\",\"id\":\"google.com\"}}";


  @Test
  public void canReadDomainBlacklistWithScopeAndRole() throws Exception {
    mockMvc.perform(get("/data/domainBlacklist")
      .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_WRITE_EMAIL_DOMAIN_BAN)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(1)));
  }

  @Test
  public void canReaDomainBlacklistWithoutScope() throws Exception {
    mockMvc.perform(get("/data/domainBlacklist")
      .with(getOAuthTokenWithTestUser(NO_SCOPE, GroupPermission.ROLE_WRITE_EMAIL_DOMAIN_BAN)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(0)));
  }

  @Test
  public void canReadDomainBlacklistWithoutRole() throws Exception {
    mockMvc.perform(get("/data/domainBlacklist")
      .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, NO_AUTHORITIES)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(0)));
  }


  @Test
  public void canReadSpecificDomainBanWithScopeAndRole() throws Exception {
    mockMvc.perform(get("/data/domainBlacklist/spam.org")
      .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_WRITE_EMAIL_DOMAIN_BAN)))
      .andExpect(status().isOk());
  }

  @Test
  public void cannotReadSpecificDomainBanWithoutScope() throws Exception {
    mockMvc.perform(get("/data/domainBlacklist/spam.org")
      .with(getOAuthTokenWithTestUser(NO_SCOPE, GroupPermission.ROLE_WRITE_EMAIL_DOMAIN_BAN)))
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotReadSpecificDomainBanWithoutRole() throws Exception {
    mockMvc.perform(get("/data/domainBlacklist/spam.org")
      .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, NO_AUTHORITIES)))
      .andExpect(status().isForbidden());
  }

  @Test
  public void canCreateDomainBanWithScopeAndRole() throws Exception {
    mockMvc.perform(
      post("/data/domainBlacklist")
        .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_WRITE_EMAIL_DOMAIN_BAN))
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(NEW_DOMAIN))
      .andExpect(status().isCreated());
  }

  @Test
  public void cannotCreateDomainBanWithoutScope() throws Exception {
    mockMvc.perform(
      post("/data/domainBlacklist")
        .with(getOAuthTokenWithTestUser(NO_SCOPE, GroupPermission.ROLE_WRITE_EMAIL_DOMAIN_BAN))
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(NEW_DOMAIN))
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotCreateDomainBanWithoutRole() throws Exception {
    mockMvc.perform(
      post("/data/domainBlacklist")
        .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(NEW_DOMAIN))
      .andExpect(status().isForbidden());
  }


  @Test
  public void canUpdateDomainBanWithScopeAndRole() throws Exception {
    mockMvc.perform(
      patch("/data/domainBlacklist/spam.org")
        .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_WRITE_EMAIL_DOMAIN_BAN))
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content("{\"data\":{\"type\":\"domainBlacklist\",\"id\":\"spam.org\"}}"))
      .andExpect(status().isNoContent());
  }

  @Test
  public void cannotUpdateDomainBanWithoutScope() throws Exception {
    mockMvc.perform(
      patch("/data/domainBlacklist/spam.org")
        .with(getOAuthTokenWithTestUser(NO_SCOPE, GroupPermission.ROLE_WRITE_EMAIL_DOMAIN_BAN))
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(NEW_DOMAIN))
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotUpdateDomainBanWithoutRole() throws Exception {
    mockMvc.perform(
      patch("/data/domainBlacklist/spam.org")
        .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, NO_AUTHORITIES))
        .header(HttpHeaders.CONTENT_TYPE, DataController.JSON_API_MEDIA_TYPE)
        .content(NEW_DOMAIN))
      .andExpect(status().isForbidden());
  }


  @Test
  public void canDeleteDomainBanWithScopeAndRole() throws Exception {
    mockMvc.perform(
      delete("/data/domainBlacklist/spam.org")
        .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, GroupPermission.ROLE_WRITE_EMAIL_DOMAIN_BAN)))
      .andExpect(status().isNoContent());
  }

  @Test
  public void cannotDeleteDomainBanWithoutScope() throws Exception {
    mockMvc.perform(
      delete("/data/domainBlacklist/spam.org")
        .with(getOAuthTokenWithTestUser(NO_SCOPE, GroupPermission.ROLE_WRITE_EMAIL_DOMAIN_BAN)))
      .andExpect(status().isForbidden());
  }

  @Test
  public void cannotDeleteDomainBanWithoutRole() throws Exception {
    mockMvc.perform(
      delete("/data/domainBlacklist/spam.org")
        .with(getOAuthTokenWithTestUser(OAuthScope._ADMINISTRATIVE_ACTION, NO_AUTHORITIES)))
      .andExpect(status().isForbidden());
  }

  @Test
  @WithUserDetails(AUTH_USER)
  public void cannotDeleteDomainBlacklistAsUser() throws Exception {
    mockMvc.perform(
      delete("/data/domainBlacklist/spam.org"))
      .andExpect(status().isForbidden());
  }
}

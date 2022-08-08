package com.faforever.api.featuredmods;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.security.OAuthScope;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesRegex;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/truncateTables.sql")
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultData.sql")
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepFeaturedMods.sql")
public class FeaturedModsControllerTest extends AbstractIntegrationTest {

  @Test
  public void featuredModFileUrlCorrectWithLobbyScope() throws Exception {
    mockMvc.perform(get("/featuredMods/0/files/latest")
             .with(getOAuthTokenWithActiveUser(OAuthScope._LOBBY, NO_AUTHORITIES)))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.data", hasSize(1)))
           .andExpect(jsonPath("$.data[0].type", is("featuredModFile")))
           .andExpect(jsonPath("$.data[0].attributes.url", matchesRegex(".*\\?verify=[0-9]+-.*")));
  }

  @Test
  public void featuredModFileNotVisibleWithoutLobbyScope() throws Exception {
    mockMvc.perform(get("/featuredMods/0/files/latest")
             .with(getOAuthTokenWithActiveUser(NO_SCOPE, NO_AUTHORITIES)))
           .andExpect(status().isForbidden());
  }
}

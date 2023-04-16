package com.faforever.api.coturn;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.security.OAuthScope;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesRegex;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/truncateTables.sql")
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultData.sql")
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepCoturnServer.sql")
public class CoturnControllerTest extends AbstractIntegrationTest {

  @Test
  public void featuredModFileUrlCorrectWithLobbyScope() throws Exception {
    mockMvc.perform(get("/coturnServers/details").with(getOAuthTokenWithActiveUser(OAuthScope._LOBBY, NO_AUTHORITIES)))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.data", hasSize(1)))
           .andExpect(jsonPath("$.data[0].type", is("coturnServerDetails")))
           .andExpect(jsonPath("$.data[0].attributes", hasKey("urls")))
           .andExpect(jsonPath("$.data[0].attributes", hasKey("username")))
           .andExpect(jsonPath("$.data[0].attributes", hasKey("credential")))
           .andExpect(jsonPath("$.data[0].attributes", hasKey("credentialType")))
           .andExpect(jsonPath("$.data[0].attributes.urls", containsInAnyOrder(equalTo("turn:test.com:3478?transport=tcp"), equalTo("turn:test.com:3478?transport=udp"), equalTo("turn:test.com:3478"))))
           .andExpect(jsonPath("$.data[0].attributes.username", matchesRegex("[0-9]+:5")))
           .andExpect(jsonPath("$.data[0].attributes.credentialType", equalTo("token")));
  }

  @Test
  public void featuredModFileNotVisibleWithoutLobbyScope() throws Exception {
    mockMvc.perform(get("/coturnServers/details").with(getOAuthTokenWithActiveUser(NO_SCOPE, NO_AUTHORITIES)))
           .andExpect(status().isForbidden());
  }
}

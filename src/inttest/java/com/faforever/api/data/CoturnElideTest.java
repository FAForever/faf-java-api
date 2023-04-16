package com.faforever.api.data;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.security.OAuthScope;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/truncateTables.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultData.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepCoturnServer.sql")
public class CoturnElideTest extends AbstractIntegrationTest {

  @Test
  public void cannotReadWithoutScope() throws Exception {
    mockMvc.perform(get("/data/coturnServer").with(getOAuthTokenWithActiveUser(NO_SCOPE, NO_AUTHORITIES)))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.data", hasSize(0)));
  }

  @Test
  public void canReadBansWithScopeAndRole() throws Exception {
    mockMvc.perform(get("/data/coturnServer").with(getOAuthTokenWithActiveUser(OAuthScope._LOBBY, NO_AUTHORITIES)))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.data", hasSize(1)))
           .andExpect(jsonPath("$.data[0].attributes", hasKey("key")))
           .andExpect(jsonPath("$.data[0].attributes", hasKey("host")))
           .andExpect(jsonPath("$.data[0].attributes", hasKey("region")))
           .andExpect(jsonPath("$.data[0].attributes", hasKey("port")))
           .andExpect(jsonPath("$.data[0].attributes", hasKey("active")));
  }
}

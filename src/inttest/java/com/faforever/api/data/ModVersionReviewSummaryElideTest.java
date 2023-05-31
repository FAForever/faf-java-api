package com.faforever.api.data;

import com.faforever.api.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/truncateTables.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultData.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepModData.sql")
class ModVersionReviewSummaryElideTest extends AbstractIntegrationTest {

  @Test
  void canReadMapVersionReviewSummaryWithoutReviews() throws Exception {
    mockMvc.perform(get("/data/modVersion/1/reviewsSummary")
      .with(getOAuthTokenWithActiveUser(NO_SCOPE, NO_AUTHORITIES)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.attributes.averageScore", nullValue()));
  }
}

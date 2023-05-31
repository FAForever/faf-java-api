package com.faforever.api.data;

import com.faforever.api.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/truncateTables.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultData.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepMapVersion.sql")
class MapReviewSummaryElideTest extends AbstractIntegrationTest {

  @Test
  void canReadMapReviewSummary() throws Exception {
    mockMvc.perform(get("/data/map/1/reviewsSummary")
      .with(getOAuthTokenWithActiveUser(NO_SCOPE, NO_AUTHORITIES)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.attributes.lowerBound", is(0.0)))
      .andExpect(jsonPath("$.data.attributes.negative", is(0.0)))
      .andExpect(jsonPath("$.data.attributes.positive", is(0.0)))
      .andExpect(jsonPath("$.data.attributes.reviews", is(1)))
      .andExpect(jsonPath("$.data.attributes.score", is(2.0)))
      .andExpect(jsonPath("$.data.attributes.averageScore", is(2.0)));
  }
}

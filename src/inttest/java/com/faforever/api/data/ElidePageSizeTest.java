package com.faforever.api.data;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.data.domain.GroupPermission;
import com.faforever.api.error.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/truncateTables.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultData.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepModData.sql")
class ElidePageSizeTest extends AbstractIntegrationTest {

  @Test
  void normalUserValidPageSize() throws Exception {
    mockMvc.perform(
        get("/data/mod")
          .queryParam("page[size]", "100")
          .with(getOAuthTokenWithActiveUser(NO_SCOPE, GroupPermission.ROLE_USER))
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data").isArray());
  }

  @Test
  void normalUserInvalidPageSize() throws Exception {
    final MvcResult result = mockMvc.perform(
        get("/data/mod")
          .queryParam("page[limit]", "101")
          .with(getOAuthTokenWithActiveUser(NO_SCOPE, GroupPermission.ROLE_USER))
      )
      .andExpect(status().isUnprocessableEntity()).andReturn();

    assertApiError(result, ErrorCode.QUERY_INVALID_PAGE_SIZE);
  }

  @Test
  void scraperUserValidPageSize() throws Exception {
    mockMvc.perform(
        get("/data/mod")
          .queryParam("page[size]", "9000")
          .with(getOAuthTokenWithActiveUser(NO_SCOPE, GroupPermission.ROLE_SCRAPER))
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data").isArray());
  }

  @Test
  void scraperUserInvalidPageSize() throws Exception {
    final MvcResult result = mockMvc.perform(
        get("/data/mod")
          .queryParam("page[limit]", "10001")
          .with(getOAuthTokenWithActiveUser(NO_SCOPE, GroupPermission.ROLE_SCRAPER))
      )
      .andExpect(status().isUnprocessableEntity()).andReturn();

    assertApiError(result, ErrorCode.QUERY_INVALID_PAGE_SIZE);
  }

  @Test
  void nanPageSize() throws Exception {
    mockMvc.perform(
        get("/data/mod")
          .queryParam("page[limit]", "invalid-not-a-number")
          .with(getOAuthTokenWithActiveUser(NO_SCOPE, GroupPermission.ROLE_SCRAPER))
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors[*]", hasSize(1)));
  }
}

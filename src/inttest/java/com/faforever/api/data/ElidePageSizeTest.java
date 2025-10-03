package com.faforever.api.data;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.data.domain.GroupPermission;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/truncateTables.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultData.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepModData.sql")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ElidePageSizeTest extends AbstractIntegrationTest {

  private static final int TEST_DEFAULT_PAGE_SIZE = 1;
  private static final int TEST_MAX_PAGE_SIZE = 2;

  @DynamicPropertySource
  static void configureRabbitProperties(DynamicPropertyRegistry registry) {
    registry.add("elide.default-page-size", () -> TEST_DEFAULT_PAGE_SIZE);
    registry.add("elide.max-page-size", () -> TEST_MAX_PAGE_SIZE);
  }

  @Test
  void normalUserValidPageSize() throws Exception {
    final String validPageSize = String.valueOf(TEST_DEFAULT_PAGE_SIZE);

    mockMvc.perform(
        get("/data/mod")
          .queryParam("page[size]", validPageSize)
          .with(getOAuthTokenWithActiveUser(NO_SCOPE, GroupPermission.ROLE_USER))
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data").isArray())
      .andExpect(jsonPath("$.data", hasSize(TEST_DEFAULT_PAGE_SIZE)));
  }

  @Test
  void normalUserInvalidPageSize() throws Exception {
    final String invalidOneOverTheLimit = String.valueOf(TEST_DEFAULT_PAGE_SIZE + 1);

    mockMvc.perform(
        get("/data/mod")
          .queryParam("page[limit]", invalidOneOverTheLimit)
          .with(getOAuthTokenWithActiveUser(NO_SCOPE, GroupPermission.ROLE_USER))
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data").isArray())
      .andExpect(jsonPath("$.data", hasSize(TEST_DEFAULT_PAGE_SIZE)));
  }

  @Test
  void scraperUserValidPageSize() throws Exception {
    final String validPageSize = String.valueOf(TEST_MAX_PAGE_SIZE);

    mockMvc.perform(
        get("/data/mod")
          .queryParam("page[size]", validPageSize)
          .with(getOAuthTokenWithActiveUser(NO_SCOPE, GroupPermission.ROLE_SCRAPER))
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data").isArray())
      .andExpect(jsonPath("$.data", hasSize(TEST_MAX_PAGE_SIZE)));
  }

  @Test
  void scraperUserInvalidPageSize() throws Exception {
    final String invalidOneOverTheLimit = String.valueOf(TEST_MAX_PAGE_SIZE + 1);

    mockMvc.perform(
        get("/data/mod")
          .queryParam("page[limit]", invalidOneOverTheLimit)
          .with(getOAuthTokenWithActiveUser(NO_SCOPE, GroupPermission.ROLE_SCRAPER))
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data").isArray())
      .andExpect(jsonPath("$.data", hasSize(TEST_MAX_PAGE_SIZE)));
  }

  @Test
  void nanPageSize() throws Exception {
    final String invalidParamValue = "invalid-not-a-number";

    mockMvc.perform(
        get("/data/mod")
          .queryParam("page[limit]", invalidParamValue)
          .with(getOAuthTokenWithActiveUser(NO_SCOPE, GroupPermission.ROLE_SCRAPER))
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.errors[*]", hasSize(1)));
  }
}

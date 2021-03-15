package com.faforever.api.data;

import com.faforever.api.AbstractIntegrationTest;
import org.junit.Test;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/truncateTables.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultData.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepModData.sql")
public class ModSolrTest extends AbstractIntegrationTest {

  private static final String MOD_SAMPLE_RESPONSE = "{\n" +
    "  \"data\": [\n" +
    "  ] \n" +
    "}";

  @Test
  public void respondForWhitespacedData() throws Exception {
    mockMvc.perform(
      get("/data/mod?filter=displayName==total music")
      .with(getOAuthTokenWithTestUser(NO_SCOPE, NO_AUTHORITIES)))
      .andExpect(status().isOk());
  }
}

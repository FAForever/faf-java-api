package com.faforever.api.data;

import com.faforever.api.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultUser.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepMapVersion.sql")
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sql/cleanMapVersion.sql")
public class MapVersionElideTest extends AbstractIntegrationTest {

  private static final String MAP_VERSION_HIDE_FALSE_ID_1 = "{\n" +
    "  \"data\": {\n" +
    "    \"type\": \"mapVersion\",\n" +
    "    \"id\": \"1\",\n" +
    "    \"attributes\": {\n" +
    "    \t\"hidden\": false\n" +
    "    }\n" +
    "  } \n" +
    "}";
  private static final String MAP_VERSION_HIDE_TRUE_ID_1 = "{\n" +
    "  \"data\": {\n" +
    "    \"type\": \"mapVersion\",\n" +
    "    \"id\": \"1\",\n" +
    "    \"attributes\": {\n" +
    "    \t\"hidden\": true\n" +
    "    }\n" +
    "  } \n" +
    "}";
  private static final String MAP_VERSION_RANKED_FALSE_ID_1 = "{\n" +
    "  \"data\": {\n" +
    "    \"type\": \"mapVersion\",\n" +
    "    \"id\": \"1\",\n" +
    "    \"attributes\": {\n" +
    "    \t\"ranked\": false\n" +
    "    }\n" +
    "  } \n" +
    "}";
  private static final String MAP_VERSION_RANKED_TRUE_ID_1 = "{\n" +
    "  \"data\": {\n" +
    "    \"type\": \"mapVersion\",\n" +
    "    \"id\": \"1\",\n" +
    "    \"attributes\": {\n" +
    "    \t\"ranked\": true\n" +
    "    }\n" +
    "  } \n" +
    "}";


  @WithUserDetails(AUTH_USER)
  @Test
  public void testUpdateHideToTrueDoesWork() throws Exception {
    mockMvc.perform(
      patch("/data/mapVersion/1")
        .header(HttpHeaders.CONTENT_TYPE, JsonApiMediaType.JSON_API_MEDIA_TYPE)
        .content(MAP_VERSION_HIDE_TRUE_ID_1))
      .andExpect(status().isNoContent());
  }

  @WithUserDetails(AUTH_USER)
  @Test
  public void testUpdateHideToFalseDoesNotWork() throws Exception {
    mockMvc.perform(
      patch("/data/mapVersion/1")
        .header(HttpHeaders.CONTENT_TYPE, JsonApiMediaType.JSON_API_MEDIA_TYPE)
        .content(MAP_VERSION_HIDE_FALSE_ID_1))
      .andExpect(status().isForbidden());
  }


  @WithUserDetails(AUTH_USER)
  @Test
  public void testUpdateRankedToFalseDoesWork() throws Exception {
    mockMvc.perform(
      patch("/data/mapVersion/1")
        .header(HttpHeaders.CONTENT_TYPE, JsonApiMediaType.JSON_API_MEDIA_TYPE)
        .content(MAP_VERSION_RANKED_FALSE_ID_1))
      .andExpect(status().isNoContent());
  }

  @WithUserDetails(AUTH_USER)
  @Test
  public void testUpdateRankedToTrueDoesNotWork() throws Exception {
    mockMvc.perform(
      patch("/data/mapVersion/1")
        .header(HttpHeaders.CONTENT_TYPE, JsonApiMediaType.JSON_API_MEDIA_TYPE)
        .content(MAP_VERSION_RANKED_TRUE_ID_1))
      .andExpect(status().isForbidden());
  }
}

package com.faforever.api.event;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.data.JsonApiMediaType;
import com.faforever.api.security.OAuthScope;
import com.google.common.collect.Lists;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepDefaultUser.sql")
@Sql(executionPhase = ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:sql/prepEventsData.sql")
@Sql(executionPhase = ExecutionPhase.AFTER_TEST_METHOD, scripts = "classpath:sql/cleanEventsData.sql")
public class EventControllerTest extends AbstractIntegrationTest {

  @Test
  public void singleExistingPlayerEventCanBeUpdated() throws Exception {
    List<EventUpdateRequest> updatedEvents = Lists.newArrayList(new EventUpdateRequest(1, "15b6c19a-6084-4e82-ada9-6c30e282191f", 10));
    mockMvc.perform(
      patch("/events/update")
        .header(HttpHeaders.CONTENT_TYPE, JsonApiMediaType.JSON_API_MEDIA_TYPE)
        .content(objectMapper.writeValueAsString(updatedEvents))
        .with(getOAuthTokenWithoutUser(OAuthScope._WRITE_EVENTS)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(1)))
      .andExpect(jsonPath("$.data[0].attributes.currentCount", Matchers.is(31)))
      .andExpect(jsonPath("$.data[0].attributes.eventId", Matchers.is("15b6c19a-6084-4e82-ada9-6c30e282191f")));
  }

  @Test
  public void singleNonExistingPlayerEventCanBeCreated() throws Exception {
    List<EventUpdateRequest> updatedEvents = Lists.newArrayList(new EventUpdateRequest(1, "cc791f00-343c-48d4-b5b3-8900b83209c0", 10));
    mockMvc.perform(
      patch("/events/update")
        .header(HttpHeaders.CONTENT_TYPE, JsonApiMediaType.JSON_API_MEDIA_TYPE)
        .content(objectMapper.writeValueAsString(updatedEvents))
        .with(getOAuthTokenWithoutUser(OAuthScope._WRITE_EVENTS)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(1)))
      .andExpect(jsonPath("$.data[0].attributes.currentCount", Matchers.is(10)))
      .andExpect(jsonPath("$.data[0].attributes.eventId", Matchers.is("cc791f00-343c-48d4-b5b3-8900b83209c0")));
  }

  @Test
  public void multipleExistingPlayerEventsCanBeUpdated() throws Exception {
    List<EventUpdateRequest> updatedEvents = Lists.newArrayList(
      new EventUpdateRequest(1, "15b6c19a-6084-4e82-ada9-6c30e282191f", 10),
      new EventUpdateRequest(1, "225e9b2e-ae09-4ae1-a198-eca8780b0fcd", 10)
    );
    mockMvc.perform(
      patch("/events/update")
        .header(HttpHeaders.CONTENT_TYPE, JsonApiMediaType.JSON_API_MEDIA_TYPE)
        .content(objectMapper.writeValueAsString(updatedEvents))
        .with(getOAuthTokenWithoutUser(OAuthScope._WRITE_EVENTS)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data", hasSize(2)))
      .andExpect(jsonPath("$.data[*].attributes.currentCount", Matchers.containsInAnyOrder(31, 20)))
      .andExpect(jsonPath("$.data[*].attributes.eventId", Matchers.containsInAnyOrder("15b6c19a-6084-4e82-ada9-6c30e282191f", "225e9b2e-ae09-4ae1-a198-eca8780b0fcd")));
  }

  @Test
  public void authWithoutCorrectScopeShouldFail() throws Exception {
    List<EventUpdateRequest> updatedEvents = Lists.newArrayList(new EventUpdateRequest(1, "15b6c19a-6084-4e82-ada9-6c30e282191f", 10));
    mockMvc.perform(
      patch("/events/update")
        .header(HttpHeaders.CONTENT_TYPE, JsonApiMediaType.JSON_API_MEDIA_TYPE)
        .content(objectMapper.writeValueAsString(updatedEvents)))
      .andExpect(status().isForbidden());
  }

  @Test
  public void nonExistingEventShouldFail() throws Exception {
    List<EventUpdateRequest> updatedEvents = Lists.newArrayList(new EventUpdateRequest(1, "non-existing-event-id", 10));
    mockMvc.perform(
      patch("/events/update")
        .header(HttpHeaders.CONTENT_TYPE, JsonApiMediaType.JSON_API_MEDIA_TYPE)
        .content(objectMapper.writeValueAsString(updatedEvents))
        .with(getOAuthTokenWithoutUser(OAuthScope._WRITE_EVENTS)))
      .andExpect(status().isUnprocessableEntity());
  }

  @Test
  public void nonExistingPlayerShouldFail() throws Exception {
    List<EventUpdateRequest> updatedEvents = Lists.newArrayList(new EventUpdateRequest(-1, "15b6c19a-6084-4e82-ada9-6c30e282191f", 10));
    mockMvc.perform(
      patch("/events/update")
        .header(HttpHeaders.CONTENT_TYPE, JsonApiMediaType.JSON_API_MEDIA_TYPE)
        .content(objectMapper.writeValueAsString(updatedEvents))
        .with(getOAuthTokenWithoutUser(OAuthScope._WRITE_EVENTS)))
      .andExpect(status().isUnprocessableEntity());
  }

  @Test
  public void negativeCountShouldFail() throws Exception {
    List<EventUpdateRequest> updatedEvents = Lists.newArrayList(new EventUpdateRequest(1, "15b6c19a-6084-4e82-ada9-6c30e282191f", -10));
    mockMvc.perform(
      patch("/events/update")
        .header(HttpHeaders.CONTENT_TYPE, JsonApiMediaType.JSON_API_MEDIA_TYPE)
        .content(objectMapper.writeValueAsString(updatedEvents))
        .with(getOAuthTokenWithoutUser(OAuthScope._WRITE_EVENTS)))
      .andExpect(status().isUnprocessableEntity());
  }
}

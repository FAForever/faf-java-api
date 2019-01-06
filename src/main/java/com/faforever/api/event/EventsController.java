package com.faforever.api.event;

import com.faforever.api.security.OAuthScope;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.yahoo.elide.jsonapi.models.Data;
import com.yahoo.elide.jsonapi.models.JsonApiDocument;
import com.yahoo.elide.jsonapi.models.Resource;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/events")
@Validated
public class EventsController {

  private static final String JSON_API_MEDIA_TYPE = "application/vnd.api+json";
  private final EventsService eventsService;

  @Inject
  public EventsController(EventsService eventsService) {
    this.eventsService = eventsService;
  }

  @ApiOperation(value = "Updates the state and progress of one or multiple events.")
  @PreAuthorize("#oauth2.hasScope('" + OAuthScope._WRITE_EVENTS + "')")
  @RequestMapping(value = "/update", method = RequestMethod.PATCH, produces = JSON_API_MEDIA_TYPE)
  public JsonApiDocument update(@RequestBody List<@Valid EventUpdateRequest> updateRequests) {
    return new JsonApiDocument(new Data<>(updateRequests.stream()
      .map(request -> eventsService.increment(request.getPlayerId(), request.getEventId(), request.getCount()))
      .map(this::toResource)
      .collect(Collectors.toList())));
  }

  private Resource toResource(UpdatedEventResponse updatedEventResponse) {
    Builder<String, Object> attributesBuilder = ImmutableMap.<String, Object>builder()
      .put("eventId", updatedEventResponse.getEventId())
      .put("currentCount", updatedEventResponse.getCurrentCount());

    return new Resource("updatedEvent", String.valueOf(updatedEventResponse.getId()),
      attributesBuilder.build(), null, null, null);
  }
}

package com.faforever.api.event;

import com.faforever.api.data.JsonApiMediaType;
import com.faforever.api.security.OAuthScope;
import com.yahoo.elide.jsonapi.models.Data;
import com.yahoo.elide.jsonapi.models.JsonApiDocument;
import com.yahoo.elide.jsonapi.models.Resource;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/events")
@Validated
@RequiredArgsConstructor
public class EventsController {

  private final EventsService eventsService;

  @ApiOperation(value = "Updates the state and progress of one or multiple events.")
  @PreAuthorize("hasScope('" + OAuthScope._WRITE_EVENTS + "')")
  @RequestMapping(value = "/update", method = RequestMethod.PATCH, produces = JsonApiMediaType.JSON_API_MEDIA_TYPE)
  public JsonApiDocument update(@RequestBody List<@Valid EventUpdateRequest> updateRequests) {
    return new JsonApiDocument(new Data<>(updateRequests.stream()
      .map(request -> eventsService.increment(request.playerId(), request.eventId(), request.count()))
      .map(this::toResource)
      .toList()));
  }

  private Resource toResource(UpdatedEventResponse updatedEventResponse) {
    Map<String, Object> attributes = Map.of(
      "eventId", updatedEventResponse.eventId(),
      "currentCount", updatedEventResponse.currentCount()
    );

    return new Resource("updatedEvent", String.valueOf(updatedEventResponse.id()),
      attributes, null, null, null);
  }
}

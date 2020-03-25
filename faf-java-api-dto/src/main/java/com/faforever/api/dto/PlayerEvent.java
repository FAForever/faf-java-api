package com.faforever.api.dto;

import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Type(PlayerEvent.TYPE)
public class PlayerEvent extends AbstractEntity {
  public static final String TYPE = "playerEvent";


  private int count;

  @Relationship("event")
  private Event event;
}

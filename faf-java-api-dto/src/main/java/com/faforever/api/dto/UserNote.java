package com.faforever.api.dto;

import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Type(UserNote.TYPE)
@RestrictedVisibility("IsModerator")
public class UserNote extends AbstractEntity {
  public static final String TYPE = "userNote";

  @Relationship("player")
  private Player player;
  @Relationship("author")
  private Player author;
  private boolean watched;
  private String note;
}

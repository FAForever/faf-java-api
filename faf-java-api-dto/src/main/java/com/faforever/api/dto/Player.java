package com.faforever.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Type(Player.TYPE)
public class Player extends AbstractEntity {
  public static final String TYPE = "player";

  private String login;

  @Relationship("names")
  List<NameRecord> names;

  @Relationship("globalRating")
  private GlobalRating globalRating;

  @Relationship("ladder1v1Rating")
  private Ladder1v1Rating ladder1v1Rating;

  @Relationship("avatarAssignments")
  @JsonIgnore
  private List<AvatarAssignment> avatarAssignments;

  @Override
  public String toString() {
    return String.format("%s [id %s]", login, id);
  }
}

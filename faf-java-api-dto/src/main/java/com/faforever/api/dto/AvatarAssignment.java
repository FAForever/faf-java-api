package com.faforever.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Type(AvatarAssignment.TYPE)
public class AvatarAssignment extends AbstractEntity {
  public static final String TYPE = "avatarAssignment";

  private Boolean selected;
  private OffsetDateTime expiresAt;
  @Relationship("player")
  @JsonIgnore
  private Player player;
  @Relationship("avatar")
  @JsonIgnore
  private Avatar avatar;
}

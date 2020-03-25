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
@Type(Avatar.TYPE)
public class Avatar extends AbstractEntity {
  public static final String TYPE = "avatar";

  private String url;
  private String tooltip;
  @Relationship("assignments")
  @JsonIgnore
  private List<AvatarAssignment> assignments;
}

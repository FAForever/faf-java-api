package com.faforever.api.dto;

import com.github.jasminb.jsonapi.annotations.Relationship;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class Review extends AbstractEntity {
  private String text;
  private Byte score;

  @Relationship("player")
  private Player player;
}

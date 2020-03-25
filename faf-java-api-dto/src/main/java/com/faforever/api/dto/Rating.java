package com.faforever.api.dto;


import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@SuperBuilder
public class Rating {
  @Id
  @EqualsAndHashCode.Include
  private String id;
  private double mean;
  private double deviation;
  private double rating;

  @Relationship("player")
  private Player player;
}

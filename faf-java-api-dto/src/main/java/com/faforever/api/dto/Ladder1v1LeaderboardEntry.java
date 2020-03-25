package com.faforever.api.dto;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@Type(Ladder1v1LeaderboardEntry.TYPE)
public class Ladder1v1LeaderboardEntry {
  public static final String TYPE = "ladder1v1LeaderboardEntry";

  @Id
  @EqualsAndHashCode.Include
  private String id;
  private int rank;
  private String name;
  private Double mean;
  private Double deviation;
  private Integer numGames;
  private Integer wonGames;
  private Boolean isActive;
  private Double rating;
}

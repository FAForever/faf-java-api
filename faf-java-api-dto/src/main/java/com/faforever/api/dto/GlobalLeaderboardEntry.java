package com.faforever.api.dto;

import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@Type(GlobalLeaderboardEntry.TYPE)
public class GlobalLeaderboardEntry {
  public static final String TYPE = "globalLeaderboardEntry";

  @Id
  @EqualsAndHashCode.Include
  private String id;
  private String name;
  private int rank;
  private Double mean;
  private Double deviation;
  private Integer numGames;
  private Boolean isActive;
  private Double rating;
}

package com.faforever.api.dto;

import com.faforever.api.elide.ElideEntity;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@Type(Game.TYPE)
public class Game implements ElideEntity {
  public static final String TYPE = "game";

  @Id
  @EqualsAndHashCode.Include
  private String id;
  private String name;
  private OffsetDateTime startTime;
  private OffsetDateTime endTime;
  private Validity validity;
  private VictoryCondition victoryCondition;

  @Relationship("reviews")
  private List<GameReview> reviews;

  @Relationship("playerStats")
  private List<GamePlayerStats> playerStats;

  @Relationship("host")
  private Player host;

  @Relationship("featuredMod")
  private FeaturedMod featuredMod;

  @Relationship("mapVersion")
  private MapVersion mapVersion;
}

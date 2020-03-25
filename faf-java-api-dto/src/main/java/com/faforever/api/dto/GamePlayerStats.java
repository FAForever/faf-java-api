package com.faforever.api.dto;

import com.faforever.api.elide.ElideEntity;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@Type(GamePlayerStats.TYPE)
public class GamePlayerStats implements ElideEntity {
  public static final String TYPE = "game";

  @Id
  @EqualsAndHashCode.Include
  private String id;
  private boolean ai;
  private Faction faction;
  private byte color;
  private byte team;
  private byte startSpot;
  private Float beforeMean;
  private Float beforeDeviation;
  private Float afterMean;
  private Float afterDeviation;
  private byte score;
  @Nullable
  private OffsetDateTime scoreTime;

  @Relationship("game")
  private Game game;

  @Relationship("player")
  private Player player;
}

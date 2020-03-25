package com.faforever.api.dto;

import com.faforever.api.elide.ElideEntity;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Duration;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@Type(CoopResult.TYPE)
public class CoopResult implements ElideEntity {
  public static final String TYPE = "coopResult";

  @Id
  @EqualsAndHashCode.Include
  private String id;
  private Duration duration;
  private String playerNames;
  private boolean secondaryObjectives;
  private int ranking;
  private int playerCount;
}

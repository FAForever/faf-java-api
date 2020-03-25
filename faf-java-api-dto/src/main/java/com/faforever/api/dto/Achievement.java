package com.faforever.api.dto;

import com.faforever.api.elide.ElideEntity;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@Type(Achievement.TYPE)
public class Achievement implements ElideEntity {
  public static final String TYPE = "achievement";

  @Id
  @EqualsAndHashCode.Include
  private String id;
  private String description;
  private int experiencePoints;
  private AchievementState initialState;
  private String name;
  private String revealedIconUrl;
  private Integer totalSteps;
  private AchievementType type;
  private String unlockedIconUrl;
  private int order;
}

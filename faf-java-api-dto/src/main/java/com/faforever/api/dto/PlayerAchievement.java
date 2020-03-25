package com.faforever.api.dto;


import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Type(PlayerAchievement.TYPE)
public class PlayerAchievement extends AbstractEntity {
  public static final String TYPE = "playerAchievement";

  private AchievementState state;
  private Integer currentSteps;

  @Relationship("achievement")
  private Achievement achievement;
}

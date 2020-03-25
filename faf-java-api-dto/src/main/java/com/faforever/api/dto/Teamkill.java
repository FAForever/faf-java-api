package com.faforever.api.dto;

import com.faforever.api.elide.ElideEntity;
import com.github.jasminb.jsonapi.annotations.Id;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@Type(Teamkill.TYPE)
@RestrictedVisibility("IsModerator")
public class Teamkill implements ElideEntity {
  public static final String TYPE = "teamkill";

  @Id
  @EqualsAndHashCode.Include
  private String id;
  @Relationship("teamkiller")
  private Player teamkiller;
  @Relationship("victim")
  private Player victim;
  @Relationship("game")
  private Game game;
  /**
   * How many seconds into the game, in simulation time.
   */
  private long gameTime;
  private OffsetDateTime reportedAt;
}

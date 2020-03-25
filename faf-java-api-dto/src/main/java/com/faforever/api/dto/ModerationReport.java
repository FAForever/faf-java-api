package com.faforever.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Type(ModerationReport.TYPE)
public class ModerationReport extends AbstractEntity {
  public static final String TYPE = "moderationReport";

  private String reportDescription;
  private ModerationReportStatus reportStatus;
  private String gameIncidentTimecode;
  private String moderatorNotice;
  private String moderatorPrivateNote;

  @Relationship("bans")
  @JsonIgnore
  @ToString.Exclude
  private Set<BanInfo> bans;
  @Relationship("reporter")
  @JsonIgnore
  private User reporter;
  @Relationship("game")
  @JsonIgnore
  private Game game;
  @Relationship("lastModerator")
  @JsonIgnore
  private User lastModerator;
  @Relationship("reportedUsers")
  @JsonIgnore
  @ToString.Exclude
  private Set<Player> reportedUsers;
}

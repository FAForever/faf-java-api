package com.faforever.api.dto;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.jasminb.jsonapi.annotations.Relationship;
import com.github.jasminb.jsonapi.annotations.Type;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Type(User.TYPE)
@RestrictedVisibility("IsModerator")
public class User extends AbstractEntity {
  public static final String TYPE = "user";


  private String login;

  private String email;

  private String userAgent;

  private String steamId;

  private String recentIpAddress;

  private OffsetDateTime lastLogin;

  @Relationship("names")
  List<NameRecord> names;

  @Relationship("bans")
  private List<BanInfo> bans;

  @Relationship("avatarAssignments")
  @JsonIgnore
  private List<AvatarAssignment> avatarAssignments;

  @JsonBackReference
  @Relationship("reporterOnModerationReports")
  private Set<ModerationReport> reporterOnModerationReports;

  @Override
  public String toString() {
    return String.format("%s [id %s]", login, id);
  }
}

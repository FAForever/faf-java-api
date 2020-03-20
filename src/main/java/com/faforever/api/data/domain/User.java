package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsEntityOwner;
import com.faforever.api.data.checks.Prefab;
import com.faforever.api.security.elide.permission.AdminAccountBanCheck;
import com.faforever.api.security.elide.permission.AdminAccountNoteCheck;
import com.faforever.api.security.elide.permission.AdminModerationReportCheck;
import com.faforever.api.security.elide.permission.ReadAccountPrivateDetailsCheck;
import com.faforever.api.security.elide.permission.ReadUserGroupCheck;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "login")
@ReadPermission(expression = ReadAccountPrivateDetailsCheck.EXPRESSION + " OR " + IsEntityOwner.EXPRESSION)
@Setter
@Include(rootLevel = true, type = User.TYPE_NAME)
public class User extends Login {
  public static final String TYPE_NAME = "user";

  private String password;
  private String email;
  private String steamId;
  private String userAgent;
  private Set<BanInfo> bans;
  private Set<UserNote> userNotes;
  private Set<UserGroup> userGroups;
  private String recentIpAddress;
  private OffsetDateTime lastLogin;
  private Set<ModerationReport> reporterOnModerationReports;
  private Set<ModerationReport> reportedOnModerationReports;

  public User() {
    this.bans = new HashSet<>(0);
    this.userNotes = new HashSet<>(0);
  }

  @Column(name = "password")
  @ReadPermission(expression = Prefab.NONE)
  public String getPassword() {
    return password;
  }

  @Column(name = "email")
  @ReadPermission(expression = IsEntityOwner.EXPRESSION + " OR " + ReadAccountPrivateDetailsCheck.EXPRESSION)
  public String getEmail() {
    return email;
  }

  @Column(name = "steamid")
  @ReadPermission(expression = IsEntityOwner.EXPRESSION + " OR " + ReadAccountPrivateDetailsCheck.EXPRESSION)
  public String getSteamId() {
    return steamId;
  }

  @Column(name = "ip")
  @ReadPermission(expression = IsEntityOwner.EXPRESSION + " OR " + ReadAccountPrivateDetailsCheck.EXPRESSION)
  public String getRecentIpAddress() {
    return recentIpAddress;
  }

  @Column(name = "last_login")
  @ReadPermission(expression = IsEntityOwner.EXPRESSION + " OR " + ReadAccountPrivateDetailsCheck.EXPRESSION)
  public OffsetDateTime getLastLogin() {
    return lastLogin;
  }

  @Column(name = "user_agent")
  public String getUserAgent() {
    return userAgent;
  }

  @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
  // Permission is managed by BanInfo class
  @UpdatePermission(expression = AdminAccountBanCheck.EXPRESSION)
  public Set<BanInfo> getBans() {
    return this.bans;
  }

  @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
  @UpdatePermission(expression = AdminAccountNoteCheck.EXPRESSION)
  public Set<UserNote> getUserNotes() {
    return this.userNotes;
  }

  @Transient
  public Set<BanInfo> getActiveBans() {
    return getBans().stream().filter(ban -> ban.getBanStatus() == BanStatus.BANNED).collect(Collectors.toSet());
  }

  @Transient
  public boolean isGlobalBanned() {
    return getActiveBans().stream().anyMatch(ban -> ban.getLevel() == BanLevel.GLOBAL);
  }

  @ReadPermission(expression = IsEntityOwner.EXPRESSION + " OR " + ReadUserGroupCheck.EXPRESSION)
  @UpdatePermission(expression = Prefab.ALL)
  @ManyToMany(mappedBy = "members")
  public Set<UserGroup> getUserGroups() {
    return userGroups;
  }

  @ReadPermission(expression = AdminModerationReportCheck.EXPRESSION + " OR " + IsEntityOwner.EXPRESSION)
  // Permission is managed by Moderation reports class
  @UpdatePermission(expression = Prefab.ALL)
  @OneToMany(mappedBy = "reporter")
  @BatchSize(size = 1000)
  public Set<ModerationReport> getReporterOnModerationReports() {
    return reporterOnModerationReports;
  }

  // Permission is managed by Moderation reports class
  @ReadPermission(expression = AdminModerationReportCheck.EXPRESSION)
  @UpdatePermission(expression = Prefab.ALL)
  @ManyToMany(mappedBy = "reportedUsers")
  public Set<ModerationReport> getReportedOnModerationReports() {
    return reportedOnModerationReports;
  }
}

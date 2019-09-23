package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsEntityOwner;
import com.faforever.api.data.checks.Prefab;
import com.faforever.api.data.checks.permission.IsModerator;
import com.faforever.api.security.elide.permission.AdminAccountBanCheck;
import com.faforever.api.security.elide.permission.AdminAccountNoteCheck;
import com.faforever.api.security.elide.permission.ReadUserGroupCheck;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@MappedSuperclass
@Setter
public abstract class Login extends AbstractEntity implements OwnableEntity {

  private String login;
  private String email;
  private String steamId;
  private String userAgent;
  private Set<BanInfo> bans;
  private Set<UserNote> userNotes;
  private Set<UserGroup> userGroups;
  private String recentIpAddress;
  private OffsetDateTime lastLogin;

  public Login() {
    this.bans = new HashSet<>(0);
    this.userNotes = new HashSet<>(0);
  }

  @Column(name = "login")
  public String getLogin() {
    return login;
  }

  @Column(name = "email")
  @ReadPermission(expression = IsEntityOwner.EXPRESSION + " OR " + IsModerator.EXPRESSION)
  public String getEmail() {
    return email;
  }

  @Column(name = "steamid")
  @ReadPermission(expression = IsEntityOwner.EXPRESSION + " OR " + IsModerator.EXPRESSION)
  public String getSteamId() {
    return steamId;
  }

  @Column(name = "ip")
  @ReadPermission(expression = IsEntityOwner.EXPRESSION + " OR " + IsModerator.EXPRESSION)
  public String getRecentIpAddress() {
    return recentIpAddress;
  }

  @Column(name = "last_login")
  @ReadPermission(expression = IsEntityOwner.EXPRESSION + " OR " + IsModerator.EXPRESSION)
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

  @Override
  @Transient
  @JsonIgnore
  public Login getEntityOwner() {
    return this;
  }
}

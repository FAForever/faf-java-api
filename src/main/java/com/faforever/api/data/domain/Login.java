package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsEntityOwner;
import com.faforever.api.data.checks.permission.IsModerator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@MappedSuperclass
@Getter
@Setter
public abstract class Login extends AbstractEntity implements OwnableEntity {

  @Column(name = "login")
  private String login;

  @Column(name = "email")
  @ReadPermission(expression = IsEntityOwner.EXPRESSION + " OR " + IsModerator.EXPRESSION)
  private String email;

  @Column(name = "steamid")
  @ReadPermission(expression = IsEntityOwner.EXPRESSION + " OR " + IsModerator.EXPRESSION)
  private String steamId;

  @Column(name = "user_agent")
  private String userAgent;

  @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
  // Permission is managed by BanInfo class
  @UpdatePermission(expression = IsModerator.EXPRESSION)
  private Set<BanInfo> bans;

  @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
  @UpdatePermission(expression = IsModerator.EXPRESSION)
  private Set<UserNote> userNotes;

  @OneToOne(mappedBy = "user")
  private LobbyGroup lobbyGroup;

  @Column(name = "ip")
  @ReadPermission(expression = IsEntityOwner.EXPRESSION + " OR " + IsModerator.EXPRESSION)
  private String recentIpAddress;

  @Column(name = "last_login")
  @ReadPermission(expression = IsEntityOwner.EXPRESSION + " OR " + IsModerator.EXPRESSION)
  private OffsetDateTime lastLogin;

  public Login() {
    this.bans = new HashSet<>(0);
    this.userNotes = new HashSet<>(0);
  }

  @Transient
  public Set<BanInfo> getActiveBans() {
    return getBans().stream().filter(ban -> ban.getBanStatus() == BanStatus.BANNED).collect(Collectors.toSet());
  }

  @Transient
  public boolean isGlobalBanned() {
    return getActiveBans().stream().anyMatch(ban -> ban.getLevel() == BanLevel.GLOBAL);
  }

  @Override
  @Transient
  @JsonIgnore
  public Login getEntityOwner() {
    return this;
  }
}

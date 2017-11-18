package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsEntityOwner;
import com.faforever.api.data.checks.permission.IsModerator;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@MappedSuperclass
@Setter
public abstract class Login extends AbstractEntity implements OwnableEntity {

  private String login;
  private String email;
  private String steamId;
  private String userAgent;
  private List<BanInfo> bans;
  private List<UserNote> userNotes;
  private LobbyGroup lobbyGroup;
  private String recentIpAddress;

  public Login() {
    this.bans = new ArrayList<>(0);
    this.userNotes = new ArrayList<>(0);
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
  @ReadPermission(expression = IsLoginOwner.EXPRESSION + " OR " + IsModerator.EXPRESSION)
  public String getRecentIpAddress() {
    return recentIpAddress;
  }

  @Column(name = "user_agent")
  public String getUserAgent() {
    return userAgent;
  }

  @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
  // Permission is managed by BanInfo class
  @UpdatePermission(expression = IsModerator.EXPRESSION)
  public List<BanInfo> getBans() {
    return this.bans;
  }

  @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
  @UpdatePermission(expression = IsModerator.EXPRESSION)
  public List<UserNote> getUserNotes() {
    return this.userNotes;
  }

  @Transient
  public List<BanInfo> getActiveBans() {
    return getBans().stream().filter(ban -> ban.getBanStatus() == BanStatus.BANNED).collect(Collectors.toList());
  }

  @Transient
  public boolean isGlobalBanned() {
    return getActiveBans().stream().anyMatch(ban -> ban.getLevel() == BanLevel.GLOBAL);
  }

  @OneToOne(mappedBy = "user")
  public LobbyGroup getLobbyGroup() {
    return lobbyGroup;
  }

  @Override
  @Transient
  public Login getEntityOwner() {
    return this;
  }
}

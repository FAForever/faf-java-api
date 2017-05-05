package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsLoginOwner;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@MappedSuperclass
@Inheritance(strategy = InheritanceType.JOINED)
@Setter
public abstract class Login {

  private int id;
  private String login;
  private String email;
  private String steamId;
  private String userAgent;
  private List<BanInfo> bans;

  @Id
  @GeneratedValue
  public int getId() {
    return id;
  }

  @Column(name = "login")
  public String getLogin() {
    return login;
  }

  @Column(name = "email")
  @ReadPermission(expression = IsLoginOwner.EXPRESSION)
  public String getEmail() {
    return email;
  }

  @Column(name = "steamid")
  @ReadPermission(expression = IsLoginOwner.EXPRESSION)
  public String getSteamId() {
    return steamId;
  }

  @Column(name = "user_agent")
  public String getUserAgent() {
    return userAgent;
  }

  @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
  // Permission is managed by BanInfo class
  @UpdatePermission(expression = "Prefab.Role.All")
  public List<BanInfo> getBans() {
    if (this.bans == null) {
      this.bans = Collections.emptyList();
    }
    return this.bans;
  }

  @Transient
  public List<BanInfo> getActiveBans() {
    return getBans().stream().filter(ban -> ban.getBanStatus() == BanStatus.BANNED).collect(Collectors.toList());
  }

  @Transient
  public boolean isGlobalBanned() {
    return getActiveBans().stream().anyMatch(ban -> ban.getLevel() == BanLevel.GLOBAL);
  }
}

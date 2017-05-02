package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsLoginOwner;
import com.yahoo.elide.annotation.ReadPermission;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

@MappedSuperclass
@Inheritance(strategy = InheritanceType.JOINED)
@Setter
public abstract class Login {

  private int id;
  private String login;
  private String email;
  private String steamId;
  private String userAgent;
  private BanInfo banInfo;

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

  @OneToOne(mappedBy = "player", fetch = FetchType.LAZY)
  public BanInfo getBanInfo() {
    return banInfo;
  }
}

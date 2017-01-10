package com.faforever.api.data.domain;

import com.faforever.api.config.elide.checks.IsOwner;
import com.yahoo.elide.annotation.ReadPermission;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

@MappedSuperclass
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Login {

  private int id;
  private String login;
  private String eMail;
  private String steamId;
  private String userAgent;
  private BanDetails banDetails;

  @Id
  @GeneratedValue
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  @Basic
  @Column(name = "login")
  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  @Basic
  @Column(name = "email")
  @ReadPermission(expression = IsOwner.EXPRESSION)
  public String getEMail() {
    return eMail;
  }

  public void setEMail(String eMail) {
    this.eMail = eMail;
  }

  @Basic
  @Column(name = "steamid")
  @ReadPermission(expression = IsOwner.EXPRESSION)
  public String getSteamId() {
    return steamId;
  }

  public void setSteamId(String steamId) {
    this.steamId = steamId;
  }

  @Basic
  @Column(name = "user_agent")
  public String getUserAgent() {
    return userAgent;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  @OneToOne(mappedBy = "player")
  public BanDetails getBanDetails() {
    return banDetails;
  }

  public void setBanDetails(BanDetails banDetails) {
    this.banDetails = banDetails;
  }
}

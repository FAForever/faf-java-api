package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsOwner;
import com.yahoo.elide.annotation.ReadPermission;
import lombok.Setter;
import org.hibernate.annotations.Formula;

import javax.persistence.Column;
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
  private String eMail;
  private String steamId;
  private String userAgent;
  private BanInfo banInfo;
  private String lowerCaseLogin;

  @Id
  @GeneratedValue
  public int getId() {
    return id;
  }

  @Column(name = "login")
  public String getLogin() {
    return login;
  }

  // Needed for filter, e.g. at the clan app
  @Formula("LOWER(login)")
  public String getLowerCaseLogin() {
    return lowerCaseLogin;
  }

  @Column(name = "email")
  @ReadPermission(expression = IsOwner.EXPRESSION)
  public String getEMail() {
    return eMail;
  }

  @Column(name = "steamid")
  @ReadPermission(expression = IsOwner.EXPRESSION)
  public String getSteamId() {
    return steamId;
  }

  @Column(name = "user_agent")
  public String getUserAgent() {
    return userAgent;
  }

  @OneToOne(mappedBy = "player")
  public BanInfo getBanInfo() {
    return banInfo;
  }
}

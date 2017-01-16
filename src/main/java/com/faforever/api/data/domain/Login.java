package com.faforever.api.data.domain;

import com.faforever.api.config.elide.checks.IsOwner;
import com.yahoo.elide.annotation.ReadPermission;
import lombok.Setter;
import org.hibernate.annotations.Formula;

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
@Setter // Don't generate toString with lombok to avoid loops
public abstract class Login {

  private int id;
  private String login;
  private String eMail;
  private String steamId;
  private String userAgent;
  private BanDetails banDetails;
  private String lowerCaseLogin;

  @Id
  @GeneratedValue
  public int getId() {
    return id;
  }

  @Basic
  @Column(name = "login")
  public String getLogin() {
    return login;
  }

  // neeeded for filter
  @Formula("LOWER(login)")
  public String getLowerCaseLogin() {
    return lowerCaseLogin;
  }

  @Basic
  @Column(name = "email")
  @ReadPermission(expression = IsOwner.EXPRESSION)
  public String getEMail() {
    return eMail;
  }

  @Basic
  @Column(name = "steamid")
  @ReadPermission(expression = IsOwner.EXPRESSION)
  public String getSteamId() {
    return steamId;
  }

  @Basic
  @Column(name = "user_agent")
  public String getUserAgent() {
    return userAgent;
  }

  @OneToOne(mappedBy = "player")
  public BanDetails getBanDetails() {
    return banDetails;
  }
}

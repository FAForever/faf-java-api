package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "login")
@Include(rootLevel = true, type = "player")
public class Player {

  private Integer id;
  private String login;
  private String eMail;
  private String steamId;
  private String userAgent;
  private Ladder1v1Rating ladder1v1Rating;
  private GlobalRating globalRating;

  @Id
  @GeneratedValue
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
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
  @ReadPermission(expression = "user is this user")
  public String getEMail() {
    return eMail;
  }

  public void setEMail(String eMail) {
    this.eMail = eMail;
  }

  @Basic
  @Column(name = "steamid")
  @ReadPermission(expression = "user is this user")
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
  public Ladder1v1Rating getLadder1v1Rating() {
    return ladder1v1Rating;
  }

  public void setLadder1v1Rating(Ladder1v1Rating ladder1v1Rating) {
    this.ladder1v1Rating = ladder1v1Rating;
  }

  @OneToOne(mappedBy = "player")
  public GlobalRating getGlobalRating() {
    return globalRating;
  }

  public void setGlobalRating(GlobalRating globalRating) {
    this.globalRating = globalRating;
  }
}

package com.faforever.api.data.domain;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "lobby_ban")
public class BanDetails implements Serializable {

  private User user;
  private String reason;
  private Timestamp expiresAt;

  @Id
  @OneToOne
  @JoinColumn(name = "idUser", updatable = false)
  public User getUser() {
    return user;
  }

  public void setUser(User idUser) {
    this.user = idUser;
  }

  @Basic
  @Column(name = "reason")
  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  @Basic
  @Column(name = "expires_at")
  public Timestamp getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(Timestamp expiresAt) {
    this.expiresAt = expiresAt;
  }

  @Override
  public int hashCode() {
    return Objects.hash(user, reason, expiresAt);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BanDetails banDetails = (BanDetails) o;
    return user == banDetails.user &&
        Objects.equals(reason, banDetails.reason) &&
        Objects.equals(expiresAt, banDetails.expiresAt);
  }
}

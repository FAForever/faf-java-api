package com.faforever.api.data.domain;

import com.faforever.api.data.checks.Prefab;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

@MappedSuperclass
@Setter
public abstract class Login extends AbstractEntity implements OwnableEntity {

  private String login;
  private ClanMembership clanMembership;

  @Column(name = "login")
  public String getLogin() {
    return login;
  }

  // Permission is managed by ClanMembership class
  @UpdatePermission(expression = Prefab.ALL)
  @OneToOne(mappedBy = "player")
  public ClanMembership getClanMembership() {
    return this.clanMembership;
  }

  @Transient
  public Clan getClan() {
    return clanMembership == null ? null : clanMembership.getClan();
  }

  @Override
  @Transient
  @JsonIgnore
  public Login getEntityOwner() {
    return this;
  }
}

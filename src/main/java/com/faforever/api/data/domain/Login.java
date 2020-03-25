package com.faforever.api.data.domain;

import com.faforever.api.data.checks.Prefab;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import java.util.Set;

@MappedSuperclass
@Setter
public abstract class Login extends AbstractEntity implements OwnableEntity {

  private String login;
  private ClanMembership clanMembership;
  private Set<AvatarAssignment> avatarAssignments;
  private Set<NameRecord> names;

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

  // Permission is managed by AvatarAssignment class
  @UpdatePermission(expression = Prefab.ALL)
  @OneToMany(mappedBy = "player")
  @BatchSize(size = 1000)
  public Set<AvatarAssignment> getAvatarAssignments() {
    return avatarAssignments;
  }

  // Permission is managed by NameRecord class
  @UpdatePermission(expression = Prefab.ALL)
  @OneToMany(mappedBy = "player")
  public Set<NameRecord> getNames() {
    return this.names;
  }

  @Override
  @Transient
  @JsonIgnore
  public Login getEntityOwner() {
    return this;
  }
}

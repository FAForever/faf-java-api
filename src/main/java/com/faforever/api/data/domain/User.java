package com.faforever.api.data.domain;

import com.faforever.api.data.checks.Prefab;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "login")
@Setter
@Include(type = "user")
public class User extends Login {
  private String password;

  @Column(name = "password")
  @ReadPermission(expression = Prefab.NONE)
  public String getPassword() {
    return password;
  }
}

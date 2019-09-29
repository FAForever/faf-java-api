package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "login")
@Getter
@Setter
@Include(type = "user")
public class User extends Login {
  @Column(name = "password")
  @ReadPermission(expression = "Prefab.Role.None")
  private String password;
}

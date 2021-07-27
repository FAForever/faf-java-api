package com.faforever.api.data.domain;

import com.faforever.api.data.checks.Prefab;
import com.yahoo.elide.annotation.ReadPermission;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "login")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class User extends Login {
  @Column(name = "password")
  @ReadPermission(expression = Prefab.NONE)
  private String password;
}

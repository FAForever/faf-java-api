package com.faforever.api.data.domain;

import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "login")
@Setter
public class User extends Login {

  private String password;

  @Column(name = "password")
  public String getPassword() {
    return password;
  }

  public boolean hasPermission(String permission) {
    return false;
  }
}

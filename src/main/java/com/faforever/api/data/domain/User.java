package com.faforever.api.data.domain;

import com.faforever.api.data.checks.Prefab;
import com.yahoo.elide.annotation.ReadPermission;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "login")
@Setter
public class User extends Login {
  private String password;

  @Column(name = "password")
  @ReadPermission(expression = Prefab.NONE)
  public String getPassword() {
    return password;
  }

  //Overriding toString method for efficient Logging
  @Override
  public String toString(){
    return "User(id = " + getId() + ", email = " + getEmail() + ")";
  }
}

package com.faforever.api.data.domain;

import lombok.Setter;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "login")
@Setter
public class User extends Login {

  private String password;

  @Basic
  @Column(name = "password")
  public String getPassword() {
    return password;
  }
}

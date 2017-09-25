package com.faforever.api.data.domain;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "lobby_admin")
@Setter
@Deprecated
@NoArgsConstructor
@AllArgsConstructor
public class LobbyGroup {
  private int userId;
  private int accessLevel;
  private User user;

  @Column(name = "\"group\"")
  public int getAccessLevel() {
    return accessLevel;
  }

  @Id
  @Column(name = "user_id")
  public int getUserId() {
    return userId;
  }

  @OneToOne
  @JoinColumn(name = "user_id")
  public User getUser() {
    return user;
  }
}

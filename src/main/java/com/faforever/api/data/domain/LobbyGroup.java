package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * @deprecated LobbyGroups are supposed to be replaced with role based security
 */
@Entity
@Table(name = "lobby_admin")
@Setter
@Deprecated
@NoArgsConstructor
@AllArgsConstructor
@Include(type = "lobbyGroup", rootLevel = false)
public class LobbyGroup {
  private int userId;
  private LegacyAccessLevel accessLevel;
  private Player user;

  @Column(name = "\"group\"")
  public LegacyAccessLevel getAccessLevel() {
    return accessLevel;
  }

  @Id
  @Column(name = "user_id")
  public int getUserId() {
    return userId;
  }

  @OneToOne
  @JoinColumn(name = "user_id", insertable = false, updatable = false)
  public Player getUser() {
    return user;
  }
}

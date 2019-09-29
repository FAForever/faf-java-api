package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.AllArgsConstructor;
import lombok.Getter;
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
@Getter
@Setter
@Deprecated
@NoArgsConstructor
@AllArgsConstructor
@Include(type = "lobbyGroup")
public class LobbyGroup {

  @Id
  @Column(name = "user_id")
  private int userId;

  @Column(name = "\"group\"")
  private LegacyAccessLevel accessLevel;

  @OneToOne
  @JoinColumn(name = "user_id", insertable = false, updatable = false)
  private User user;
}

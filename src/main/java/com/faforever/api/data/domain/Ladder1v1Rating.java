package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Setter
@Table(name = "ladder1v1_rating_rank_view")
@Include(name = "ladder1v1Rating")
public class Ladder1v1Rating extends Rating {
  private int wonGames;

  @Column(name = "win_games", updatable = false)
  public int getWonGames() {
    return wonGames;
  }
}

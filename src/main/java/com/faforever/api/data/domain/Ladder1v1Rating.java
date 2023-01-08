package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

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

package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Setter
@Include(rootLevel = true, type = "gameReview")
@Entity
@Table(name = "game_review")
@PrimaryKeyJoinColumn(name = "review_id", referencedColumnName = "id")
public class GameReview extends Review {
  private Game game;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "game_id")
  public Game getGame() {
    return game;
  }
}

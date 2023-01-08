package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Exclude;
import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "player_events")
@Include(name = "playerEvent")
@Setter
public class PlayerEvent extends AbstractEntity<PlayerEvent> {

  private int playerId;
  private Player player;
  private Event event;
  private int currentCount;

  @OneToOne
  @JoinColumn(name = "player_id", insertable = false, updatable = false)
  public Player getPlayer() {
    return player;
  }

  @Exclude
  @Column(name = "player_id")
  public int getPlayerId() {
    return playerId;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "event_id", updatable = false)
  public Event getEvent() {
    return event;
  }

  @Column(name = "count")
  public int getCurrentCount() {
    return currentCount;
  }
}

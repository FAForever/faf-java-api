package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Exclude;
import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "player_events")
@Include(rootLevel = true, type = com.faforever.api.dto.PlayerEvent.TYPE)
@Setter
public class PlayerEvent extends AbstractEntity {

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

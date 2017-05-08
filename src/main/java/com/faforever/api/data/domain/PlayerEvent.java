package com.faforever.api.data.domain;

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
@Include(rootLevel = true, type = "playerEvent")
@Setter
public class PlayerEvent extends AbstractEntity {

  private Player player;
  private Event event;
  private int count;

  @OneToOne
  @JoinColumn(name = "player_id")
  public Player getPlayer() {
    return player;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "event_id", updatable = false, insertable = false)
  public Event getEvent() {
    return event;
  }

  @Column(name = "count")
  public int getCount() {
    return count;
  }
}

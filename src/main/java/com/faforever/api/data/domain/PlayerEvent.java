package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Exclude;
import com.yahoo.elide.annotation.Include;
import lombok.Getter;
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
@Getter
@Setter
public class PlayerEvent extends AbstractEntity {

  @Exclude
  @Column(name = "player_id")
  private int playerId;

  @OneToOne
  @JoinColumn(name = "player_id", insertable = false, updatable = false)
  private Player player;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "event_id", updatable = false)
  private Event event;

  @Column(name = "count")
  private int currentCount;
}

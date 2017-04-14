package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "player_events")
@Include(rootLevel = true, type = "playerEvent")
@Setter
public class PlayerEvent {

  private int id;
  private Player player;
  private EventDefinition eventDefinition;
  private int count;
  private OffsetDateTime createTime;
  private OffsetDateTime updateTime;

  @Id
  @Column(name = "id")
  public int getId() {
    return id;
  }

  @OneToOne
  @JoinColumn(name = "player_id")
  public Player getPlayer() {
    return player;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "event_id", updatable = false, insertable = false)
  public EventDefinition getEventDefinition() {
    return eventDefinition;
  }

  @Column(name = "count")
  public int getCount() {
    return count;
  }

  @Column(name = "create_time")
  public OffsetDateTime getCreateTime() {
    return createTime;
  }

  @Column(name = "update_time")
  public OffsetDateTime getUpdateTime() {
    return updateTime;
  }
}

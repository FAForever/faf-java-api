package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "player_events")
@Include(rootLevel = true, type = "playerEvent")
@Setter
public class PlayerEvent {

  private int id;
  private int playerId;
  private EventDefinition eventDefinition;
  private int count;
  private Timestamp createTime;
  private Timestamp updateTime;

  @Id
  @Column(name = "id")
  public int getId() {
    return id;
  }

  @Column(name = "player_id")
  public int getPlayerId() {
    return playerId;
  }

  @ManyToOne
  @JoinColumn(name = "event_id", updatable = false, insertable = false)
  public EventDefinition getEventDefinition() {
    return eventDefinition;
  }

  @Column(name = "count")
  public int getCount() {
    return count;
  }

  @Column(name = "create_time")
  public Timestamp getCreateTime() {
    return createTime;
  }

  @Column(name = "update_time")
  public Timestamp getUpdateTime() {
    return updateTime;
  }
}

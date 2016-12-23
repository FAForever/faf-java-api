package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "map")
@Include(rootLevel = true, type = "map")
public class MapEntity {

  private Integer id;
  private String displayName;
  private String mapType;
  private String battleType;
  private Timestamp createTime;
  private Timestamp updateTime;

  @Id
  @Column(name = "id")
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  @Basic
  @Column(name = "display_name")
  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  @Basic
  @Column(name = "map_type")
  public String getMapType() {
    return mapType;
  }

  public void setMapType(String mapType) {
    this.mapType = mapType;
  }

  @Basic
  @Column(name = "battle_type")
  public String getBattleType() {
    return battleType;
  }

  public void setBattleType(String battleType) {
    this.battleType = battleType;
  }

  @Basic
  @Column(name = "create_time")
  public Timestamp getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Timestamp createTime) {
    this.createTime = createTime;
  }

  @Basic
  @Column(name = "update_time")
  public Timestamp getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(Timestamp updateTime) {
    this.updateTime = updateTime;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, displayName, mapType, battleType, createTime, updateTime);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MapEntity mapEntity = (MapEntity) o;
    return Objects.equals(id, mapEntity.id) &&
        Objects.equals(displayName, mapEntity.displayName) &&
        Objects.equals(mapType, mapEntity.mapType) &&
        Objects.equals(battleType, mapEntity.battleType) &&
        Objects.equals(createTime, mapEntity.createTime) &&
        Objects.equals(updateTime, mapEntity.updateTime);
  }
}

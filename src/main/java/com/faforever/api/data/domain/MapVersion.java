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
@Table(name = "map_version")
@Include(rootLevel = true, type = "map_version")
public class MapVersion {

  private Integer id;
  private String description;
  private Integer maxPlayers;
  private int width;
  private int height;
  private int version;
  private String filename;
  private byte ranked;
  private byte hidden;
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
  @Column(name = "description")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Basic
  @Column(name = "max_players")
  public Integer getMaxPlayers() {
    return maxPlayers;
  }

  public void setMaxPlayers(Integer maxPlayers) {
    this.maxPlayers = maxPlayers;
  }

  @Basic
  @Column(name = "width")
  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  @Basic
  @Column(name = "height")
  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  @Basic
  @Column(name = "version")
  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  @Basic
  @Column(name = "filename")
  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  @Basic
  @Column(name = "ranked")
  public byte getRanked() {
    return ranked;
  }

  public void setRanked(byte ranked) {
    this.ranked = ranked;
  }

  @Basic
  @Column(name = "hidden")
  public byte getHidden() {
    return hidden;
  }

  public void setHidden(byte hidden) {
    this.hidden = hidden;
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
    return Objects.hash(id, description, maxPlayers, width, height, version, filename, ranked, hidden, createTime, updateTime);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MapVersion that = (MapVersion) o;
    return width == that.width &&
        height == that.height &&
        version == that.version &&
        ranked == that.ranked &&
        hidden == that.hidden &&
        Objects.equals(id, that.id) &&
        Objects.equals(description, that.description) &&
        Objects.equals(maxPlayers, that.maxPlayers) &&
        Objects.equals(filename, that.filename) &&
        Objects.equals(createTime, that.createTime) &&
        Objects.equals(updateTime, that.updateTime);
  }
}

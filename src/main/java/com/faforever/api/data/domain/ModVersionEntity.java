package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "mod_version")
@Include(rootLevel = true, type = "mod_version")
public class ModVersionEntity {

  private Integer id;
  private String uid;
  private Serializable type;
  private String description;
  private short version;
  private String filename;
  private String icon;
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
  @Column(name = "uid")
  public String getUid() {
    return uid;
  }

  public void setUid(String uid) {
    this.uid = uid;
  }

  @Basic
  @Column(name = "type")
  public Serializable getType() {
    return type;
  }

  public void setType(Serializable type) {
    this.type = type;
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
  @Column(name = "version")
  public short getVersion() {
    return version;
  }

  public void setVersion(short version) {
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
  @Column(name = "icon")
  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
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
    return Objects.hash(id, uid, type, description, version, filename, icon, ranked, hidden, createTime, updateTime);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ModVersionEntity that = (ModVersionEntity) o;
    return version == that.version &&
        ranked == that.ranked &&
        hidden == that.hidden &&
        Objects.equals(id, that.id) &&
        Objects.equals(uid, that.uid) &&
        Objects.equals(type, that.type) &&
        Objects.equals(description, that.description) &&
        Objects.equals(filename, that.filename) &&
        Objects.equals(icon, that.icon) &&
        Objects.equals(createTime, that.createTime) &&
        Objects.equals(updateTime, that.updateTime);
  }
}

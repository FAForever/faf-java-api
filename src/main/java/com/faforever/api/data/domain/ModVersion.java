package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "mod_version")
@Include(rootLevel = true, type = "modVersion")
@Setter
public class ModVersion {

  private Integer id;
  private String uid;
  private ModType type;
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

  @Column(name = "uid")
  public String getUid() {
    return uid;
  }

  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  public ModType getType() {
    return type;
  }

  @Column(name = "description")
  public String getDescription() {
    return description;
  }

  @Column(name = "version")
  public short getVersion() {
    return version;
  }

  @Column(name = "filename")
  public String getFilename() {
    return filename;
  }

  @Column(name = "icon")
  public String getIcon() {
    return icon;
  }

  @Column(name = "ranked")
  public byte getRanked() {
    return ranked;
  }

  @Column(name = "hidden")
  public byte getHidden() {
    return hidden;
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

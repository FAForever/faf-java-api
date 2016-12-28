package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import org.hibernate.annotations.Immutable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "\"mod\"")
@Include(rootLevel = true, type = "mod")
@Immutable
public class Mod {

  private Integer id;
  private String displayName;
  private String author;
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
  @Column(name = "author")
  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
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
    return Objects.hash(id, displayName, author, createTime, updateTime);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Mod mod = (Mod) o;
    return Objects.equals(id, mod.id) &&
        Objects.equals(displayName, mod.displayName) &&
        Objects.equals(author, mod.author) &&
        Objects.equals(createTime, mod.createTime) &&
        Objects.equals(updateTime, mod.updateTime);
  }
}

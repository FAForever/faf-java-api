package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "\"mod\"")
@Include(rootLevel = true, type = "mod")
@Immutable
@Setter
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

  @Column(name = "display_name")
  public String getDisplayName() {
    return displayName;
  }

  @Column(name = "author")
  public String getAuthor() {
    return author;
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

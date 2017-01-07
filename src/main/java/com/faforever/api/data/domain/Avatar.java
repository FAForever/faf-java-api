package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Data;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "avatars_list")
@Include(rootLevel = true, type = "avatar")
@Data
public class Avatar {

  private int id;
  private String url;
  private String tooltip;

  @Id
  @Column(name = "id")
  public int getId() {
    return id;
  }

  @Basic
  @Column(name = "url")
  public String getUrl() {
    return url;
  }

  @Basic
  @Column(name = "tooltip")
  public String getTooltip() {
    return tooltip;
  }
}

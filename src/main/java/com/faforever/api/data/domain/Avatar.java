package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "avatars_list")
@Include(rootLevel = true, type = "avatar")
@Setter
public class Avatar {

  private int id;
  private String url;
  private String tooltip;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
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

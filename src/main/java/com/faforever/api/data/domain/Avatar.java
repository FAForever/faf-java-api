package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "avatars_list")
@Include(rootLevel = true, type = "avatar")
@Setter
public class Avatar extends AbstractEntity {

  private String url;
  private String tooltip;

  @Column(name = "url")
  public String getUrl() {
    return url;
  }

  @Column(name = "tooltip")
  public String getTooltip() {
    return tooltip;
  }
}

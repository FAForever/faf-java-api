package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Setter
@Include(rootLevel = true, type = "mapReview")
@Entity
@Table(name = "map_review")
@PrimaryKeyJoinColumn(name = "review_id", referencedColumnName = "id")
public class MapReview extends Review {
  private MapVersion mapVersion;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "map_version_id")
  public MapVersion getMapVersion() {
    return mapVersion;
  }
}

package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsEntityOwner;
import com.faforever.api.data.checks.Prefab;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Setter
@Include(rootLevel = true, type = "mapVersionReview")
@Entity
@Table(name = "map_version_review")
@CreatePermission(expression = Prefab.ALL)
@DeletePermission(expression = IsEntityOwner.EXPRESSION)
public class MapVersionReview extends Review {

  private MapVersion mapVersion;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "map_version_id")
  @UpdatePermission(expression = Prefab.ALL_AND_UPDATE_ON_CREATE)
  public MapVersion getMapVersion() {
    return mapVersion;
  }
}

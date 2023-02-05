package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsEntityOwner;
import com.faforever.api.data.checks.Prefab;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Setter
@Include(name = "mapVersionReview")
@Entity
@Table(name = "map_version_review")
@CreatePermission(expression = Prefab.ALL)
@DeletePermission(expression = IsEntityOwner.EXPRESSION)
public class MapVersionReview extends Review {

  private MapVersion mapVersion;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "map_version_id")
  @UpdatePermission(expression = Prefab.ALL)
  public MapVersion getMapVersion() {
    return mapVersion;
  }
}

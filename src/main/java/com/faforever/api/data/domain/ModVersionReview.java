package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsReviewOwner;
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
@Include(rootLevel = true, type = "modVersionReview")
@Entity
@Table(name = "mod_version_review")
@CreatePermission(expression = "Prefab.Role.All")
@DeletePermission(expression = IsReviewOwner.EXPRESSION)
public class ModVersionReview extends Review {

  private ModVersion modVersion;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mod_version_id")
  @UpdatePermission(expression = "Prefab.Role.All and Prefab.Common.UpdateOnCreate")
  public ModVersion getModVersion() {
    return modVersion;
  }
}

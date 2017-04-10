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
@Include(rootLevel = true, type = "modReview")
@Entity
@Table(name = "mod_review")
@PrimaryKeyJoinColumn(name = "review_id", referencedColumnName = "id")
public class ModReview extends Review {
  private ModVersion modVersion;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mod_version_id")
  public ModVersion getModVersion() {
    return modVersion;
  }
}

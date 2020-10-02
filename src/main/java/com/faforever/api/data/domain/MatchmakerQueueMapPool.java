package com.faforever.api.data.domain;

import com.faforever.api.security.elide.permission.WriteMatchmakerMapCheck;
import com.yahoo.elide.annotation.Audit;
import com.yahoo.elide.annotation.Audit.Action;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Set;

@CreatePermission(expression = WriteMatchmakerMapCheck.EXPRESSION)
@DeletePermission(expression = WriteMatchmakerMapCheck.EXPRESSION)
@Entity
@Setter
@Table(name = "matchmaker_queue_map_pool")
@Include(rootLevel = true, type = MatchmakerQueueMapPool.TYPE_NAME)
public class MatchmakerQueueMapPool extends AbstractEntity {

  public static final String TYPE_NAME = "matchmakerQueueMapPool";

  private MatchmakerQueue matchmakerQueue;
  private Double minRating;
  private Double maxRating;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "matchmaker_queue_id")
  public MatchmakerQueue getMatchmakerQueue() {
    return matchmakerQueue;
  }

  @Nullable
  @Column(name = "min_rating")
  public Double getMinRating() {
    return minRating;
  }

  @Nullable
  @Column(name = "max_rating")
  public Double getMaxRating() {
    return maxRating;
  }

}

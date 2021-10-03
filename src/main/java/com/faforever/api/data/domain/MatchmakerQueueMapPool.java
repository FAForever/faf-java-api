package com.faforever.api.data.domain;

import com.faforever.api.security.elide.permission.WriteMatchmakerMapCheck;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Setter;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@CreatePermission(expression = WriteMatchmakerMapCheck.EXPRESSION)
@DeletePermission(expression = WriteMatchmakerMapCheck.EXPRESSION)
@Entity
@Setter
@Table(name = "matchmaker_queue_map_pool")
@Include(name = MatchmakerQueueMapPool.TYPE_NAME)
public class MatchmakerQueueMapPool extends AbstractEntity<MatchmakerQueueMapPool> {

  public static final String TYPE_NAME = "matchmakerQueueMapPool";

  private MatchmakerQueue matchmakerQueue;
  private Double minRating;
  private Double maxRating;
  private MapPool mapPool;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "matchmaker_queue_id")
  public MatchmakerQueue getMatchmakerQueue() {
    return matchmakerQueue;
  }

  @Nullable
  @Column(name = "min_rating")
  @UpdatePermission(expression = WriteMatchmakerMapCheck.EXPRESSION)
  public Double getMinRating() {
    return minRating;
  }

  @Nullable
  @Column(name = "max_rating")
  @UpdatePermission(expression = WriteMatchmakerMapCheck.EXPRESSION)
  public Double getMaxRating() {
    return maxRating;
  }

  @OneToOne
  @JoinColumn(name = "map_pool_id")
  @UpdatePermission(expression = WriteMatchmakerMapCheck.EXPRESSION)
  public MapPool getMapPool() {
    return mapPool;
  }
}

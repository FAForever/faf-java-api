package com.faforever.api.data.domain;

import com.faforever.api.security.elide.permission.WriteMatchmakerMapCheck;
import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.DeletePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.OffsetDateTime;

@CreatePermission(expression = WriteMatchmakerMapCheck.EXPRESSION)
@DeletePermission(expression = WriteMatchmakerMapCheck.EXPRESSION)
@Entity
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@Table(name = "matchmaker_queue_map_pool")
@Include(name = MatchmakerQueueMapPool.TYPE_NAME)
public class MatchmakerQueueMapPool implements DefaultEntity {

  public static final String TYPE_NAME = "matchmakerQueueMapPool";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  @EqualsAndHashCode.Include
  private Integer id;

  @Column(name = "create_time")
  private OffsetDateTime createTime;

  @Column(name = "update_time")
  private OffsetDateTime updateTime;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "matchmaker_queue_id")
  private MatchmakerQueue matchmakerQueue;

  @Nullable
  @Column(name = "min_rating")
  @UpdatePermission(expression = WriteMatchmakerMapCheck.EXPRESSION)
  private Double minRating;

  @Nullable
  @Column(name = "max_rating")
  @UpdatePermission(expression = WriteMatchmakerMapCheck.EXPRESSION)
  private Double maxRating;

  @OneToOne
  @JoinColumn(name = "map_pool_id")
  @UpdatePermission(expression = WriteMatchmakerMapCheck.EXPRESSION)
  private MapPool mapPool;
}

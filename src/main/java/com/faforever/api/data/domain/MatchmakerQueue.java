package com.faforever.api.data.domain;

import com.faforever.api.data.checks.IsEntityOwner;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.UpdatePermission;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "matchmaker_queue")
@Include(name = MatchmakerQueue.TYPE_NAME)
public class MatchmakerQueue implements DefaultEntity {

  public static final String TYPE_NAME = "matchmakerQueue";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Integer id;

  @Column(name = "create_time")
  private OffsetDateTime createTime;

  @Column(name = "update_time")
  private OffsetDateTime updateTime;

  @Column(name = "technical_name")
  @NotNull
  @UpdatePermission(expression = IsEntityOwner.EXPRESSION)
  private String technicalName;

  @Column(name = "name_key")
  @NotNull
  @UpdatePermission(expression = IsEntityOwner.EXPRESSION)
  private String nameKey;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "featured_mod_id")
  @EqualsAndHashCode.Exclude
  private FeaturedMod featuredMod;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "leaderboard_id")
  @EqualsAndHashCode.Exclude
  private Leaderboard leaderboard;

}

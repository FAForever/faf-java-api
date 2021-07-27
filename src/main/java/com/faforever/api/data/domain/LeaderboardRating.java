package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.time.OffsetDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "leaderboard_rating")
@Include(name = LeaderboardRating.TYPE_NAME)
public class LeaderboardRating implements DefaultEntity, OwnableEntity {

  public static final String TYPE_NAME = "leaderboardRating";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Integer id;

  @Column(name = "create_time")
  private OffsetDateTime createTime;

  @Column(name = "update_time")
  private OffsetDateTime updateTime;

  @Column(name = "mean")
  private Double mean;

  @Column(name = "deviation")
  private Double deviation;

  @Column(name = "rating")
  private double rating;

  @Column(name = "total_games")
  private int totalGames;

  @Column(name = "won_games")
  private int wonGames;

  @ManyToOne
  @JoinColumn(name = "leaderboard_id")
  private Leaderboard leaderboard;

  @ManyToOne
  @JoinColumn(name = "login_id")
  private Player player;

  @Override
  @Transient
  public Login getEntityOwner() {
    return player;
  }
}

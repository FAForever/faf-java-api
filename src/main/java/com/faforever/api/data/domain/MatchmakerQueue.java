package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Setter
@Table(name = "matchmaker_queue")
@Include(name = MatchmakerQueue.TYPE_NAME)
public class MatchmakerQueue extends AbstractEntity<MatchmakerQueue> {

  public static final String TYPE_NAME = "matchmakerQueue";

  private String technicalName;
  private String nameKey;
  private String params;
  private FeaturedMod featuredMod;
  private Leaderboard leaderboard;
  private int teamSize;

  @Column(name = "technical_name")
  @NotNull
  public String getTechnicalName() {
    return technicalName;
  }

  @Column(name = "name_key")
  @NotNull
  public String getNameKey() {
    return nameKey;
  }

  @Column(name = "params")
  @NotNull
  public String getParams() {
    return params;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "featured_mod_id")
  public FeaturedMod getFeaturedMod() {
    return featuredMod;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "leaderboard_id")
  public Leaderboard getLeaderboard() {
    return leaderboard;
  }

  @Column(name = "team_size")
  @NotNull
  public int getTeamSize() {
    return teamSize;
  }
}

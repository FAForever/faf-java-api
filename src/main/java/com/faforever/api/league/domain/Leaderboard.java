package com.faforever.api.league.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Setter
@Table(name = "leaderboard")
@Include(name = com.faforever.api.league.domain.Leaderboard.TYPE_NAME)
public class Leaderboard {
  public static final String TYPE_NAME = "leagueLeaderboard";

  private Integer id;
  private String technicalName;

  @Id
  @Column(name = "id")
  public Integer getId() {
    return id;
  }


  @Column(name = "technical_name")
  public String getTechnicalName() {
    return technicalName;
  }

}

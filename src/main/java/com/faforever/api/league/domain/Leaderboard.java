package com.faforever.api.league.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Setter
@Table(name = "leaderboard")
@Include(type = com.faforever.api.league.domain.Leaderboard.TYPE_NAME)
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

package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "coop_leaderboard")
@Include(rootLevel = true, type = CoopResult.TYPE_NAME)
@Getter
@Setter
public class CoopResult {

  public static final String TYPE_NAME = "coopResult";

  @Id
  @Column(name = "id")
  private int id;

  @Column(name = "mission")
  private short mission;

  @OneToOne
  @JoinColumn(name = "gameuid")
  private Game game;

  @Column(name = "secondary")
  private boolean secondaryObjectives;

  @Column(name = "time")
  @Convert(converter = TimeConverter.class)
  private long duration;

  @Column(name = "player_count")
  private short playerCount;
}

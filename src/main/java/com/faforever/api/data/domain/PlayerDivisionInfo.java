package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "ladder_division_score")
@Include
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "league", "player", "score"})
public class PlayerDivisionInfo {

  private int id;
  private int season;
  private Player player;
  private int league;
  private float score;
  private int games;
  private Division division;

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public int getId() {
    return id;
  }

  @Column(name = "season")
  public int getSeason() {
    return season;
  }

  @ManyToOne
  @JoinColumn(name = "user_id")
  public Player getPlayer() {
    return player;
  }

  @Column(name = "league")
  public int getLeague() {
    return league;
  }

  @Column(name = "score")
  public float getScore() {
    return score;
  }

  @Column(name = "games")
  public int getGames() {
    return games;
  }

  @ManyToOne
  @JoinColumnsOrFormulas({
    @JoinColumnOrFormula(formula = @JoinFormula(value = "(SELECT d.id from ladder_division d WHERE d.league = league AND score <= d.threshold ORDER BY d.threshold DESC LIMIT 1)", referencedColumnName = "id")),
  })
  public Division getDivision() {
    return division;
  }
}

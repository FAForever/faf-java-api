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
@Include(rootLevel = true)
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "league", "player", "score"})
public class PlayerDivisionInfo {

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(name = "season")
  private int season;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private Player player;

  @Column(name = "league")
  private int league;

  @Column(name = "score")
  private float score;

  @Column(name = "games")
  private int games;

  @ManyToOne
  @JoinColumnsOrFormulas({
    @JoinColumnOrFormula(formula = @JoinFormula(value = "(SELECT d.id from ladder_division d WHERE d.league = league AND score <= d.threshold ORDER BY d.threshold DESC LIMIT 1)", referencedColumnName = "id")),
  })
  private Division division;
}

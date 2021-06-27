package com.faforever.api.data.domain;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

@MappedSuperclass
@Data
@NoArgsConstructor
public abstract class Rating {

  @Id
  @Column(name = "id")
  private int id;

  @Column(name = "ranking")
  private int ranking;

  @Column(name = "mean")
  private Double mean;

  @Column(name = "deviation")
  private Double deviation;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id", updatable = false, insertable = false)
  private Player player;

  @Column(name = "rating", updatable = false, insertable = false)
  @Generated(GenerationTime.ALWAYS)
  private Double rating;

  @Column(name = "num_games", updatable = false)
  private int numberOfGames;
}

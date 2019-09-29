package com.faforever.api.data.domain;


import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Getter
@Setter
public abstract class Rating {

  @Id
  @Column(name = "id")
  private int id;

  @Column(name = "mean")
  private Double mean;

  @Column(name = "deviation")
  private Double deviation;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "id", updatable = false, insertable = false)
  private Player player;

  @Column(name = "rating", updatable = false)
  @Generated(GenerationTime.ALWAYS)
  private double rating;
}

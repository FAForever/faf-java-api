package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ladder_division")
@Include(rootLevel = true)
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "league", "name", "threshold"}, includeFieldNames = false)
public class Division {

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "league")
  private int league;

  @Column(name = "threshold")
  private int threshold;
}

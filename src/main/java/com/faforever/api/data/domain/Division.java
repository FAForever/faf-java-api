package com.faforever.api.data.domain;

import com.yahoo.elide.annotation.Include;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ladder_division")
@Include
@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "league", "name", "threshold"}, includeFieldNames = false)
public class Division {
  private int id;
  private String name;
  private int league;
  private int threshold;

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public int getId() {
    return id;
  }

  @Column(name = "name", nullable = false)
  public String getName() {
    return name;
  }

  @Column(name = "league")
  public int getLeague() {
    return league;
  }

  @Column(name = "threshold")
  public int getThreshold() {
    return threshold;
  }
}

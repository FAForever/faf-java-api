package com.faforever.api.league.domain;

import com.yahoo.elide.annotation.ComputedAttribute;
import com.yahoo.elide.annotation.Include;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Setter
@EntityListeners(LeagueSeasonDivisionSubdivisionEnricher.class)
@Table(name = "league_season_division_subdivision")
@Include(name = LeagueSeasonDivisionSubdivision.TYPE_NAME)
public class LeagueSeasonDivisionSubdivision {
  public static final String TYPE_NAME = "leagueSeasonDivisionSubdivision";

  private Integer id;
  private LeagueSeasonDivision leagueSeasonDivision;
  private Integer subdivisionIndex;
  private String nameKey;
  private String descriptionKey;
  private Double minRating;
  private Double maxRating;
  private Integer highestScore;
  private String imageUrl;
  private String mediumImageUrl;
  private String smallImageUrl;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  public Integer getId() {
    return id;
  }

  @ManyToOne
  @JoinColumn(name = "league_season_division_id")
  public LeagueSeasonDivision getLeagueSeasonDivision() {
    return leagueSeasonDivision;
  }

  @Column(name = "subdivision_index")
  public Integer getSubdivisionIndex() {
    return subdivisionIndex;
  }

  @Column(name = "name_key")
  public String getNameKey() {
    return nameKey;
  }

  @Column(name = "description_key")
  public String getDescriptionKey() {
    return descriptionKey;
  }

  @Column(name = "min_rating")
  public Double getMinRating() {
    return minRating;
  }

  @Column(name = "max_rating")
  public Double getMaxRating() {
    return maxRating;
  }

  @Column(name = "highest_score")
  public Integer getHighestScore() {
    return highestScore;
  }

  @Transient
  @ComputedAttribute
  public String getImageUrl() {
    return imageUrl;
  }

  @Transient
  @ComputedAttribute
  public String getMediumImageUrl() {
    return mediumImageUrl;
  }

  @Transient
  @ComputedAttribute
  public String getSmallImageUrl() {
    return smallImageUrl;
  }
}

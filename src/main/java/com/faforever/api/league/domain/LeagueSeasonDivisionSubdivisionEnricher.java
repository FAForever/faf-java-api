package com.faforever.api.league.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.persistence.PostLoad;

@Component
@Slf4j
public class LeagueSeasonDivisionSubdivisionEnricher {

  @PostLoad
  public void enhance(LeagueSeasonDivisionSubdivision subdivision) {
    League league = subdivision.getLeagueSeasonDivision().getLeagueSeason().getLeague();
    String divisionName = subdivision.getLeagueSeasonDivision().getNameKey() + subdivision.getNameKey();

    subdivision.setImageUrl(league.getImageUrl() + divisionName + ".png");
    subdivision.setMediumImageUrl(league.getMediumImageUrl() + divisionName + "_medium.png");
    subdivision.setSmallImageUrl(league.getSmallImageUrl() + divisionName + "_small.png");
  }
}

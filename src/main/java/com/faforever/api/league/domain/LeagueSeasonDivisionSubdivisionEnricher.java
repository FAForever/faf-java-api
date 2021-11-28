package com.faforever.api.league.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.persistence.PostLoad;

@Component
@Slf4j
public class LeagueSeasonDivisionSubdivisionEnricher {

  @PostLoad
  public void enhance(LeagueSeasonDivisionSubdivision subdivision) {
    League league = subdivision.getLeagueSeasonDivision().getLeagueSeason().getLeague();
    String filename = subdivision.getLeagueSeasonDivision().getNameKey() + subdivision.getNameKey() + ".png";

    subdivision.setImageUrl(league.getImageUrl() + filename);
    subdivision.setMediumImageUrl(league.getMediumImageUrl() + filename);
    subdivision.setSmallImageUrl(league.getSmallImageUrl() + filename);
  }
}

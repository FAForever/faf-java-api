package com.faforever.api.data.listeners;

import com.faforever.api.clan.ClanService;
import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.Clan;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;

@Component
public class ClanEnricherListener {

  private static FafApiProperties fafApiProperties;
  private static ClanService clanService;

  @Inject
  public void init(FafApiProperties fafApiProperties, ClanService clanService) {
    ClanEnricherListener.fafApiProperties = fafApiProperties;
    ClanEnricherListener.clanService = clanService;
  }

  @PrePersist
  public void prePersist(Clan clan) {
    if (clan.getId() == null) {
      clanService.preCreate(clan);
    }
  }

  @PostLoad
  public void enrich(Clan clan) {
    clan.setWebsiteUrl(String.format(fafApiProperties.getClan().getWebsiteUrlFormat(), clan.getId()));
  }
}

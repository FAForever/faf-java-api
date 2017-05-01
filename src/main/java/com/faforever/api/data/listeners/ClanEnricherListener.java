package com.faforever.api.data.listeners;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.Clan;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.persistence.PostLoad;

@Component
public class ClanEnricherListener {

  private static FafApiProperties fafApiProperties;

  @Inject
  public void init(FafApiProperties fafApiProperties) {
    ClanEnricherListener.fafApiProperties = fafApiProperties;
  }

  @PostLoad
  public void enrich(Clan clan) {
    clan.setWebsiteUrl(String.format(fafApiProperties.getClan().getWebsiteUrlFormat(), clan.getId()));
  }
}

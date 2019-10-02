package com.faforever.api.data.domain;

import com.faforever.api.config.FafApiProperties;
import org.springframework.stereotype.Component;

import javax.persistence.PostLoad;

@Component
public class FeaturedModEnricher {
  private static FafApiProperties fafApiProperties;

  public void init(FafApiProperties fafApiProperties) {
    FeaturedModEnricher.fafApiProperties = fafApiProperties;
  }

  @PostLoad
  public void enrich(FeaturedMod featuredMod) {
    String technicalName = featuredMod.getTechnicalName();
    String bireusUrlFormat = fafApiProperties.getFeaturedMod().getBireusUrlFormat();

    featuredMod.setBireusUrl(String.format(bireusUrlFormat, technicalName));
  }
}

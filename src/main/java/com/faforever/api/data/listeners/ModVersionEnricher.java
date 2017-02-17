package com.faforever.api.data.listeners;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.ModVersion;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.persistence.PostLoad;

@Component
public class ModVersionEnricher {

  private static FafApiProperties apiProperties;

  @Inject
  public void init(FafApiProperties apiProperties) {
    ModVersionEnricher.apiProperties = apiProperties;
  }

  @PostLoad
  public void enhance(ModVersion modVersion) {
    String filename = modVersion.getFilename();
    modVersion.setThumbnailUrl(String.format(apiProperties.getMod().getPreviewUrlFormat(), filename.replace("mods/", "").replace(".zip", ".png")));
    modVersion.setDownloadUrl(String.format(apiProperties.getMod().getDownloadUrlFormat(), filename.replace("mods/", "")));
  }
}

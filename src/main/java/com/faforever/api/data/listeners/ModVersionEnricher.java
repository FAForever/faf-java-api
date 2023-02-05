package com.faforever.api.data.listeners;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.ModVersion;
import org.springframework.stereotype.Component;

import jakarta.inject.Inject;
import jakarta.persistence.PostLoad;

import static com.faforever.api.mod.ModService.MOD_PATH_PREFIX;

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
    modVersion.setThumbnailUrl(String.format(
        apiProperties.getMod().getPreviewUrlFormat(),
        filename.replace(MOD_PATH_PREFIX, "").replace(".zip", ".png")
    ));
    modVersion.setDownloadUrl(String.format(
        apiProperties.getMod().getDownloadUrlFormat(),
        filename.replace(MOD_PATH_PREFIX, "")
    ));
  }
}

package com.faforever.api.data.listeners;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.CoopMap;
import org.springframework.stereotype.Component;

import jakarta.inject.Inject;
import jakarta.persistence.PostLoad;

@Component
public class CoopMapEnricher {

  private static FafApiProperties fafApiProperties;

  @Inject
  public void init(FafApiProperties fafApiProperties) {
    CoopMapEnricher.fafApiProperties = fafApiProperties;
  }

  @PostLoad
  public void enhance(CoopMap coopMap) {
    String filename = coopMap.getFilename();
    coopMap.setFolderName(filename.substring(filename.indexOf('/') + 1, filename.indexOf(".zip")));
    coopMap.setDownloadUrl(String.format(fafApiProperties.getMap().getDownloadUrlFormat(), filename.replace("maps/", "")));
    coopMap.setThumbnailUrlSmall(String.format(fafApiProperties.getMap().getSmallPreviewsUrlFormat(), filename.replace("maps/", "").replace(".zip", ".png")));
    coopMap.setThumbnailUrlLarge(String.format(fafApiProperties.getMap().getLargePreviewsUrlFormat(), filename.replace("maps/", "").replace(".zip", ".png")));
  }
}

package com.faforever.api.data.listeners;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.MapVersion;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.persistence.PostLoad;

@Component
public class MapVersionEnricher {

  private static FafApiProperties apiProperties;

  @Inject
  public void init(FafApiProperties apiProperties) {
    MapVersionEnricher.apiProperties = apiProperties;
  }

  @PostLoad
  public void enhance(MapVersion mapVersion) {
    String filename = mapVersion.getFilename();
    mapVersion.setDownloadUrl(String.format(apiProperties.getMap().getDownloadUrlFormat(), filename.replace("maps/", "")));
    mapVersion.setThumbnailUrlLarge(String.format(apiProperties.getMap().getLargePreviewsUrlFormat(), filename.replace("maps/", "").replace(".zip", ".png")));
    mapVersion.setThumbnailUrlSmall(String.format(apiProperties.getMap().getSmallPreviewsUrlFormat(), filename.replace("maps/", "").replace(".zip", ".png")));
    mapVersion.setFolderName(filename.substring(filename.indexOf('/') + 1, filename.indexOf(".zip")));
  }
}

package com.faforever.api.data.listeners;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.CoopMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.persistence.PostLoad;

@Slf4j
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
    coopMap.setFolderName(filename.substring(filename.indexOf('/') + 1, filename.indexOf(".zip") - 6));
    coopMap.setDownloadUrl(String.format(fafApiProperties.getMap().getDownloadUrlFormat(), filename.replace("/maps", "")));
    coopMap.setThumbnailUrlSmall(String.format(fafApiProperties.getMap().getSmallPreviewsUrlFormat(), filename.replace("/missions", "").replace(".zip", ".png")));
    coopMap.setThumbnailUrlLarge(String.format(fafApiProperties.getMap().getLargePreviewsUrlFormat(), filename.replace("/missions", "").replace(".zip", ".png")));
  }
}

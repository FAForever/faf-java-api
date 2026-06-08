package com.faforever.api.data.listeners;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.MapVersion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.inject.Inject;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;

@Component
@Slf4j
public class MapVersionEnricher {

  private static FafApiProperties apiProperties;
  private static EntityCacheEvictor cacheEvictor;

  @Inject
  public void init(FafApiProperties apiProperties, EntityCacheEvictor cacheEvictor) {
    MapVersionEnricher.apiProperties = apiProperties;
    MapVersionEnricher.cacheEvictor = cacheEvictor;
  }

  @PostLoad
  public void enhance(MapVersion mapVersion) {
    String filename = mapVersion.getFilename();
    mapVersion.setDownloadUrl(String.format(apiProperties.getMap().getDownloadUrlFormat(), filename.replace("maps/", "")));
    mapVersion.setThumbnailUrlLarge(String.format(apiProperties.getMap().getLargePreviewsUrlFormat(), filename.replace("maps/", "").replace(".zip", ".png")));
    mapVersion.setThumbnailUrlSmall(String.format(apiProperties.getMap().getSmallPreviewsUrlFormat(), filename.replace("maps/", "").replace(".zip", ".png")));
    mapVersion.setFolderName(filename.substring(filename.indexOf('/') + 1, filename.indexOf(".zip")));
  }

  @PostUpdate
  @PostRemove
  public void mapVersionChanged(MapVersion mapVersion) {
    log.debug("Map and MapVersion cache evicted, due to change on MapVersion with id: {}", mapVersion.getId());
    cacheEvictor.evictMapAndMapVersionCaches();
  }
}

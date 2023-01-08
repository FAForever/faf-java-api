package com.faforever.api.data.listeners;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.Avatar;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

import jakarta.inject.Inject;
import jakarta.persistence.PostLoad;
import java.nio.charset.StandardCharsets;

@Component
public class AvatarEnricherListener {

  private static FafApiProperties fafApiProperties;

  @Inject
  public void init(FafApiProperties fafApiProperties) {
    AvatarEnricherListener.fafApiProperties = fafApiProperties;
  }

  @PostLoad
  public void enrich(Avatar avatar) {
    String url = String.format(fafApiProperties.getAvatar().getDownloadUrlFormat(), avatar.getFilename());
    avatar.setUrl(UriUtils.encodePath(url, StandardCharsets.UTF_8));
  }

}

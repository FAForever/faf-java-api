package com.faforever.api.data.listeners;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.Avatar;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.persistence.PostLoad;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class AvatarEnricherListener {

  private static FafApiProperties fafApiProperties;

  @Inject
  public void init(FafApiProperties fafApiProperties) {
    AvatarEnricherListener.fafApiProperties = fafApiProperties;
  }

  @PostLoad
  public void enrich(Avatar avatar) throws UnsupportedEncodingException {
    String encodedFileName = URLEncoder.encode(avatar.getFilename(), StandardCharsets.UTF_8.toString());
    String url = String.format(fafApiProperties.getAvatar().getBaseUrl(), encodedFileName);
    avatar.setUrl(url);
  }

}

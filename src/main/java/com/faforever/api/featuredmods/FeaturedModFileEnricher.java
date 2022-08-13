package com.faforever.api.featuredmods;

import com.faforever.api.cloudflare.CloudflareService;
import com.faforever.api.config.FafApiProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.inject.Inject;
import javax.persistence.PostLoad;

@Component
public class FeaturedModFileEnricher {

  private static final String HMAC_SHA256 = "HmacSHA256";

  private static FafApiProperties fafApiProperties;
  private static CloudflareService cloudflareService;

  @Inject
  public void init(FafApiProperties fafApiProperties, CloudflareService cloudflareService) {
    FeaturedModFileEnricher.fafApiProperties = fafApiProperties;
    FeaturedModFileEnricher.cloudflareService = cloudflareService;
  }

  @PostLoad
  public void enhance(FeaturedModFile featuredModFile) {
    String folder = featuredModFile.getFolderName();
    String urlFormat = fafApiProperties.getFeaturedMod().getFileUrlFormat();
    String urlString = urlFormat.formatted(folder, featuredModFile.getOriginalFileName());

    String hmacToken = cloudflareService.generateCloudFlareHmacToken(urlString);
    String hmacParam = fafApiProperties.getCloudflare().getHmacParam();

    featuredModFile.setUrl(UriComponentsBuilder.fromUriString(urlString).queryParam(hmacParam, hmacToken).build().toString());
    featuredModFile.setCacheableUrl(urlString);
    featuredModFile.setHmacToken(hmacToken);
    featuredModFile.setHmacParameter(hmacParam);
  }
}

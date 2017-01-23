package com.faforever.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "faf-api", ignoreUnknownFields = false)
public class FafApiProperties {

  @Data
  public static class OAuth2 {

    private String resourceId = "faf-api";
  }

  @Data
  public static class Jwt {

    private int accessTokenValiditySeconds = 3600;
    private int refreshTokenValiditySeconds = 3600;
  }

  @Data
  public static class Async {

    private int corePoolSize = Runtime.getRuntime().availableProcessors();
    private int maxPoolSize = Runtime.getRuntime().availableProcessors() * 4;
    private int queueCapacity = Integer.MAX_VALUE;
  }

  @Data
  public static class Map {

    private String smallPreviewsUrlFormat = "http://content.faforever.com/faf/map_previews/small/%s";
    private String largePreviewsUrlFormat = "http://content.faforever.com/faf/map_previews/large/%s";
    private String downloadUrlFormat = "http://content.faforever.com/faf/vault/maps/%s";
    private String temporaryDirectory = "d:/tmp/faf/map";
    private String finalDirectory = "d:/tmp/faf/map_final";
    private String MapPreviewPathSmall = "d:/tmp/faf/preview_small";
    private String MapPreviewPathLarge = "d:/tmp/faf/preview_large";
    private int PreviewSizeSmall = 128;
    private int PreviewSizeLarge = 512;
    private String[] allowedExtensions = new String[]{".zip"};

  }

  /**
   * The secret used for JWT token generation.
   */
  private String jwtSecret = "banana";
  private String version = "dev";
  private Jwt jwt = new Jwt();
  private OAuth2 oAuth2 = new OAuth2();
  private Async async = new Async();
  private Map map = new Map();
}

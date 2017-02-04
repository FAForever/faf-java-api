package com.faforever.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

@Data
@ConfigurationProperties(prefix = "faf-api", ignoreUnknownFields = false)
public class FafApiProperties {
  /**
   * The secret used for JWT token generation.
   */
  private String jwtSecret = "banana";
  private String version = "dev";
  private Jwt jwt = new Jwt();
  private OAuth2 oAuth2 = new OAuth2();
  private Async async = new Async();
  private Map map = new Map();
  private Clan clan = new Clan();
  private FeaturedMods featuredMods = new FeaturedMods();

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
    private Path folderZipFiles = Paths.get("/content/faf/vault/maps");
    private Path folderPreviewPathSmall = Paths.get("/content/faf/vault/map_previews/small");
    private Path folderPreviewPathLarge = Paths.get("/content/faf/vault/map_previews/large");
    private int previewSizeSmall = 128;
    private int previewSizeLarge = 512;
    private String[] allowedExtensions = new String[]{"zip"};
  }

  @Data
  public static class FeaturedMods {
    private String fileUrlFormat = "http://content.faforever.com/faf/updaterNew/%s/%s";
  }

  @Data
  public static class Clan {
    private long inviteLinkExpireDurationInMinutes = Duration.ofDays(3).toMinutes();
  }
}

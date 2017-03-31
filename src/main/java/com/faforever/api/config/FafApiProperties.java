package com.faforever.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "faf-api", ignoreUnknownFields = false)
public class FafApiProperties {
  /**
   * The API version.
   */
  private String version;
  private Jwt jwt = new Jwt();
  private OAuth2 oAuth2 = new OAuth2();
  private Async async = new Async();
  private Map map = new Map();
  private Mod mod = new Mod();
  private Replay replay = new Replay();
  private Clan clan = new Clan();
  private FeaturedMods featuredMods = new FeaturedMods();
  private GitHub gitHub = new GitHub();
  private Deployment deployment = new Deployment();

  @Data
  public static class OAuth2 {
    private String resourceId = "faf-api";
  }

  @Data
  public static class Jwt {
    /**
     * The secret used for JWT token generation.
     */
    private String secret;
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
    /**
     * For instance {@code http://content.faforever.com/faf/vault/map_previews/small/%s}
     */
    private String smallPreviewsUrlFormat;
    /**
     * For instance {@code http://content.faforever.com/faf/vault/map_previews/large/%s}
     */
    private String largePreviewsUrlFormat;
    /**
     * For instance {@code http://content.faforever.com/faf/vault/maps/%s}
     */
    private String downloadUrlFormat;
    /**
     * The directory in which map files are stored.
     */
    private Path targetDirectory = Paths.get("static/maps");
    /**
     * The directory in which small map previews are stored.
     */
    private Path directoryPreviewPathSmall = Paths.get("static/map_previews/small");
    /**
     * The directory in which large map previews are stored.
     */
    private Path directoryPreviewPathLarge = Paths.get("static/map_previews/large");
    /**
     * The size (in pixels) of small map previews.
     */
    private int previewSizeSmall = 128;
    /**
     * The size (in pixels) of large map previews.
     */
    private int previewSizeLarge = 512;
    /**
     * Allowed file extensions of uploaded maps.
     */
    private String[] allowedExtensions = {"zip"};
  }

  @Data
  public static class Mod {
    private String previewUrlFormat;
    private String downloadUrlFormat;
  }

  @Data
  public static class Replay {
    private String downloadUrlFormat;
  }

  @Data
  public static class FeaturedMods {
    private String fileUrlFormat;
  }

  @Data
  public static class Clan {
    private long inviteLinkExpireDurationInMinutes = Duration.ofDays(3).toMinutes();
  }

  @Data
  public static class GitHub {
    private String webhookSecret;
    private String accessToken;
    private String deploymentEnvironment;
  }

  @Data
  public static class Deployment {
    private String featuredModsTargetDirectory;
    private String repositoriesDirectory;
    private String filesDirectoryFormat = "updates_%s_files";
    private String forgedAllianceExePath;
    private List<DeploymentConfiguration> configurations = new ArrayList<>();

    @Data
    public static class DeploymentConfiguration {
      private String repositoryUrl;
      private String branch;
      private String modName;
      private String modFilesExtension;
      private boolean replaceExisting;
    }
  }
}

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
  private FeaturedMod featuredMod = new FeaturedMod();
  private GitHub gitHub = new GitHub();
  private Deployment deployment = new Deployment();
  private Registration registration = new Registration();
  private PasswordReset passwordReset = new PasswordReset();
  private LinkToSteam linkToSteam = new LinkToSteam();
  private Steam steam = new Steam();
  private Mail mail = new Mail();
  private Challonge challonge = new Challonge();
  private User user = new User();
  private Database database = new Database();

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
     * The directory in which uploaded map files are stored.
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
    /** Allowed file extensions of uploaded mods. */
    private String[] allowedExtensions = {"zip"};
    /** The directory in which uploaded mod files are stored. */
    private Path targetDirectory = Paths.get("static/mods");
    /** The directory in which thumbnails of uploaded mod files are stored. */
    private Path thumbnailTargetDirectory = Paths.get("static/mod_thumbnails");
    /** The maximum allowed length of a mod's name. */
    private int maxNameLength = 100;
  }

  @Data
  public static class Replay {
    private String downloadUrlFormat;
  }

  @Data
  public static class FeaturedMod {
    private String fileUrlFormat;
    private String bireusUrlFormat;
  }

  @Data
  public static class Clan {
    private long inviteLinkExpireDurationMinutes = Duration.ofDays(7).toMinutes();
    private String websiteUrlFormat;
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
  }

  @Data
  public static class Mail {
    private String fromEmailAddress;
    private String fromEmailName;
    private String mandrillApiKey;
  }

  @Data
  public static class Registration {
    private long linkExpirationSeconds = Duration.ofDays(1).getSeconds();
    private String activationUrlFormat;
    private String subject;
    private String htmlFormat;
    private String successRedirectUrl;
  }

  @Data
  public static class PasswordReset {
    private long linkExpirationSeconds = Duration.ofDays(1).getSeconds();
    private String passwordResetUrlFormat;
    private String subject;
    private String htmlFormat;
    private String successRedirectUrl;
  }

  @Data
  public static class LinkToSteam {
    private String steamRedirectUrlFormat;
    private String successRedirectUrl;
    private String errorRedirectUrlFormat;
  }

  @Data
  public static class Steam {
    String realm;
    String apiKey;
    String forgedAllianceAppId = "9420";
    String loginUrlFormat = "https://steamcommunity.com/openid/login?%s";
    String getOwnedGamesUrlFormat = "https://api.steampowered.com/IPlayerService/GetOwnedGames/v0001?%s";
  }

  @Data
  public static class Challonge {
    private String baseUrl = "https://api.challonge.com";
    private String key;
  }

  @Data
  public static class User {
    private int minimumDaysBetweenUsernameChange = 30;
    private int usernameReservationTimeInMonths = 6;
  }

  @Data
  public static class Database {
    /**
     * The database schema version required to run this application.
     */
    private String schemaVersion;
  }
}

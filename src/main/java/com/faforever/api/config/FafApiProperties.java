package com.faforever.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Set;

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
  private Avatar avatar = new Avatar();
  private Clan clan = new Clan();
  private FeaturedMod featuredMod = new FeaturedMod();
  private GitHub gitHub = new GitHub();
  private Deployment deployment = new Deployment();
  private Registration registration = new Registration();
  private PasswordReset passwordReset = new PasswordReset();
  private Steam steam = new Steam();
  private Mail mail = new Mail();
  private Challonge challonge = new Challonge();
  private User user = new User();
  private Database database = new Database();
  private Mautic mautic = new Mautic();
  private Anope anope = new Anope();
  private Rating rating = new Rating();
  private Tutorial tutorial = new Tutorial();
  private Nodebb nodebb = new Nodebb();

  @Data
  public static class OAuth2 {
    private String resourceId = "faf-api";
  }

  @Data
  public static class Jwt {
    /**
     * The secret used for JWT token generation.
     */
    private Path secretKeyPath;
    private Path publicKeyPath;
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
    private Set<String> allowedExtensions = Set.of("zip");
  }

  @Data
  public static class Mod {
    private String previewUrlFormat;
    private String downloadUrlFormat;
    /** Allowed file extensions of uploaded mods. */
    private Set<String> allowedExtensions = Set.of("zip");
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
  public static class Avatar {
    private String downloadUrlFormat;
    private Set<String> allowedExtensions = Set.of("png");
    private Path targetDirectory;
    private int maxSizeBytes = 4096;
    private int maxNameLength = 100;
    private int imageWidth = 40;
    private int imageHeight = 20;
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
    private String testingExeUploadKey;
    private String allowedExeExtension = "exe";
    private String forgedAllianceBetaExePath;
    private String forgedAllianceDevelopExePath;
  }

  @Data
  public static class Mail {
    private String fromEmailAddress;
    private String fromEmailName;
    private Smtp smtp;
  }

  @Data
  public static class Registration {
    private long linkExpirationSeconds = Duration.ofDays(7).getSeconds();
    private String activationUrlFormat;
    private String subject;
    private String htmlFormat;
  }

  @Data
  public static class PasswordReset {
    private long linkExpirationSeconds = Duration.ofDays(7).getSeconds();
    private String passwordResetUrlFormat;
    private String subject;
    private String htmlFormat;
  }

  @Data
  public static class Steam {
    private String realm;
    private String apiKey;
    private String forgedAllianceAppId = "9420";
    private String loginUrlFormat = "https://steamcommunity.com/openid/login";
    private String getOwnedGamesUrlFormat = "https://api.steampowered.com/IPlayerService/GetOwnedGames/v0001?key={key}&steamid={steamId}&format={format}&appids_filter[0]={faAppId}";
    private String linkToSteamRedirectUrlFormat;
    private String steamPasswordResetRedirectUrlFormat;
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

  @Data
  public static class Mautic {
    private String baseUrl;
    private String clientId;
    private String clientSecret;
    private String accessTokenUrl;
  }

  @Data
  public static class Smtp {
    private String host;
    private Integer port;
    private String user;
    private String password;
  }

  @Data
  public static class Anope {
    private String databaseName;
  }

  @Data
  public static class Rating {
    private int defaultMean;
    private int defaultDeviation;
  }

  @Data
  public static class Tutorial {
    private String thumbnailUrlFormat;
  }

  @Data
  public static class Nodebb {
    private String baseUrl;
    /**
     * The nodeBB user id to be impersonated. Id 1 as initial admin should be sufficient.
     */
    private int adminUserId;
    private String masterToken;
  }
}

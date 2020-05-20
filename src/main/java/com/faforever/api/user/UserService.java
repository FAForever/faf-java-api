package com.faforever.api.user;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.GlobalRating;
import com.faforever.api.data.domain.Ladder1v1Rating;
import com.faforever.api.data.domain.NameRecord;
import com.faforever.api.data.domain.User;
import com.faforever.api.email.EmailService;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.mautic.MauticService;
import com.faforever.api.player.PlayerRepository;
import com.faforever.api.rating.GlobalRatingRepository;
import com.faforever.api.rating.Ladder1v1RatingRepository;
import com.faforever.api.security.FafPasswordEncoder;
import com.faforever.api.security.FafTokenService;
import com.faforever.api.security.FafTokenType;
import com.faforever.api.security.FafUserDetails;
import com.google.common.collect.ImmutableMap;
import com.google.common.hash.Hashing;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.faforever.api.error.ErrorCode.TOKEN_INVALID;

@Service
@Slf4j
public class UserService {
  static final String KEY_USERNAME = "username";
  static final String KEY_EMAIL = "email";
  static final String KEY_PASSWORD = "password";
  static final String KEY_USER_ID = "id";
  static final String KEY_STEAM_LINK_CALLBACK_URL = "callbackUrl";
  private static final Pattern USERNAME_PATTERN = Pattern.compile("[A-Za-z][A-Za-z0-9_-]{2,15}$");
  private final EmailService emailService;
  private final PlayerRepository playerRepository;
  private final UserRepository userRepository;
  private final NameRecordRepository nameRecordRepository;
  private final FafApiProperties properties;
  private final FafPasswordEncoder passwordEncoder;
  private final AnopeUserRepository anopeUserRepository;
  private final FafTokenService fafTokenService;
  private final SteamService steamService;
  private final Optional<MauticService> mauticService;
  private final GlobalRatingRepository globalRatingRepository;
  private final Ladder1v1RatingRepository ladder1v1RatingRepository;

  public UserService(EmailService emailService, PlayerRepository playerRepository, UserRepository userRepository,
                     NameRecordRepository nameRecordRepository, FafApiProperties properties,
                     AnopeUserRepository anopeUserRepository, FafTokenService fafTokenService,
                     SteamService steamService, Optional<MauticService> mauticService, GlobalRatingRepository globalRatingRepository, Ladder1v1RatingRepository ladder1v1RatingRepository) {
    this.emailService = emailService;
    this.playerRepository = playerRepository;
    this.userRepository = userRepository;
    this.nameRecordRepository = nameRecordRepository;
    this.properties = properties;
    this.anopeUserRepository = anopeUserRepository;
    this.fafTokenService = fafTokenService;
    this.steamService = steamService;
    this.mauticService = mauticService;
    this.globalRatingRepository = globalRatingRepository;
    this.ladder1v1RatingRepository = ladder1v1RatingRepository;
    this.passwordEncoder = new FafPasswordEncoder();
  }

  void register(String username, String email) {
    log.debug("Registration requested for user: {}", username);
    validateUsername(username);
    emailService.validateEmailAddress(email);

    if (userRepository.existsByEmail(email)) {
      throw new ApiException(new Error(ErrorCode.EMAIL_REGISTERED, email));
    }

    int usernameReservationTimeInMonths = properties.getUser().getUsernameReservationTimeInMonths();
    nameRecordRepository.getLastUsernameOwnerWithinMonths(username, usernameReservationTimeInMonths)
      .ifPresent(reservedByUserId -> {
        throw new ApiException(new Error(ErrorCode.USERNAME_RESERVED, username, usernameReservationTimeInMonths));
      });

    String token = fafTokenService.createToken(FafTokenType.REGISTRATION,
      Duration.ofSeconds(properties.getRegistration().getLinkExpirationSeconds()),
      ImmutableMap.of(
        KEY_USERNAME, username,
        KEY_EMAIL, email
      ));

    String activationUrl = String.format(properties.getRegistration().getActivationUrlFormat(), token);

    emailService.sendActivationMail(username, email, activationUrl);
  }

  private void validateUsername(String username) {
    if (!USERNAME_PATTERN.matcher(username).matches()) {
      throw new ApiException(new Error(ErrorCode.USERNAME_INVALID, username));
    }
    if (userRepository.existsByLogin(username)) {
      throw new ApiException(new Error(ErrorCode.USERNAME_TAKEN, username));
    }
  }

  /**
   * Creates a new user based on the information in the activation token.
   *
   * @param registrationToken the JWT in the format: <pre>
   *   {
   *     "action": "activate",
   *     "expiry": "2011-12-03T10:15:30Z",
   *     "
   *   }
   * </pre>
   */
  @SneakyThrows
  @SuppressWarnings("unchecked")
  @Transactional
  void activate(String registrationToken, String password, String ipAddress) {
    Map<String, String> claims = fafTokenService.resolveToken(FafTokenType.REGISTRATION, registrationToken);

    String username = claims.get(KEY_USERNAME);
    String email = claims.get(KEY_EMAIL);

    // the username could have been taken in the meantime
    validateUsername(username);

    User user = new User();
    user.setPassword(passwordEncoder.encode(password));
    user.setEmail(email);
    user.setLogin(username);
    user.setRecentIpAddress(ipAddress);

    user = userRepository.save(user);

    // @Deprecated
    // TODO: Move this db activity to the server (upcert instead of update) */
    // >>>
    double mean = properties.getRating().getDefaultMean();
    double deviation = properties.getRating().getDefaultDeviation();

    globalRatingRepository.save((GlobalRating) new GlobalRating()
      .setId(user.getId())
      .setMean(mean)
      .setDeviation(deviation));

    ladder1v1RatingRepository.save((Ladder1v1Rating) new Ladder1v1Rating()
      .setId(user.getId())
      .setMean(mean)
      .setDeviation(deviation));
    // <<<

    log.debug("User has been activated: {}", user);

    createOrUpdateMauticContact(user, ipAddress);
  }

  void changePassword(String currentPassword, String newPassword, User user) {
    if (!Objects.equals(user.getPassword(), passwordEncoder.encode(currentPassword))) {
      throw new ApiException(new Error(ErrorCode.PASSWORD_CHANGE_FAILED_WRONG_PASSWORD));
    }

    setPassword(user, newPassword);
  }

  @Transactional
  public void changeLogin(String newLogin, User user, String ipAddress) {
    internalChangeLogin(newLogin, user, ipAddress, false);
  }

  @Transactional
  public void changeLoginForced(String newLogin, User user, String ipAddress) {
    internalChangeLogin(newLogin, user, ipAddress, true);
  }

  private void internalChangeLogin(String newLogin, User user, String ipAddress, boolean force) {
    validateUsername(newLogin);

    if (!force) {
      int minDaysBetweenChange = properties.getUser().getMinimumDaysBetweenUsernameChange();
      nameRecordRepository.getDaysSinceLastNewRecord(user.getId(), minDaysBetweenChange)
        .ifPresent(daysSinceLastRecord -> {
          throw new ApiException(new Error(ErrorCode.USERNAME_CHANGE_TOO_EARLY, minDaysBetweenChange - daysSinceLastRecord.intValue() + 1));
        });

      int usernameReservationTimeInMonths = properties.getUser().getUsernameReservationTimeInMonths();
      nameRecordRepository.getLastUsernameOwnerWithinMonths(newLogin, usernameReservationTimeInMonths)
        .ifPresent(reservedByUserId -> {
          if (!reservedByUserId.equals(user.getId())) {
            throw new ApiException(new Error(ErrorCode.USERNAME_RESERVED, newLogin, usernameReservationTimeInMonths));
          }
        });

    }
    log.debug("Changing username for user ''{}'' to ''{}'', forced:''{}''", user, newLogin, force);
    NameRecord nameRecord = new NameRecord()
      .setName(user.getLogin())
      .setPlayer(playerRepository.getOne(user.getId()));
    nameRecordRepository.save(nameRecord);

    user.setLogin(newLogin);

    createOrUpdateMauticContact(userRepository.save(user), ipAddress);
  }

  private void createOrUpdateMauticContact(User user, String ipAddress) {
    mauticService.ifPresent(service -> service.createOrUpdateContact(
      user.getEmail(),
      String.valueOf(user.getId()),
      user.getLogin(),
      ipAddress,
      OffsetDateTime.now()
    )
      .thenAccept(result -> log.debug("Updated contact in Mautic: {}", user.getEmail()))
      .exceptionally(throwable -> {
        log.warn("Could not update contact in Mautic: {}", user, throwable);
        return null;
      }));
  }

  public void changeEmail(String currentPassword, String newEmail, User user, String ipAddress) {
    if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
      throw new ApiException(new Error(ErrorCode.EMAIL_CHANGE_FAILED_WRONG_PASSWORD));
    }

    emailService.validateEmailAddress(newEmail);

    log.debug("Changing email for user ''{}'' to ''{}''", user, newEmail);
    user.setEmail(newEmail);
    createOrUpdateUser(user, ipAddress);
  }

  private void createOrUpdateUser(User user, String ipAddress) {
    createOrUpdateMauticContact(userRepository.save(user), ipAddress);
  }

  void requestPasswordPasswordReset(String identifier) {
    log.debug("Password reset requested for user-identifier: {}", identifier);

    User user = userRepository.findOneByLogin(identifier)
      .orElseGet(() -> userRepository.findOneByEmail(identifier)
        .orElseThrow(() -> new ApiException(new Error(ErrorCode.UNKNOWN_IDENTIFIER))));

    String token = fafTokenService.createToken(FafTokenType.PASSWORD_RESET,
      Duration.ofSeconds(properties.getRegistration().getLinkExpirationSeconds()),
      ImmutableMap.of(KEY_USER_ID, String.valueOf(user.getId())));

    String passwordResetUrl = String.format(properties.getPasswordReset().getPasswordResetUrlFormat(), user.getLogin(), token);

    emailService.sendPasswordResetMail(user.getLogin(), user.getEmail(), passwordResetUrl);
  }

  @SneakyThrows
  void performPasswordReset(String token, String newPassword) {
    log.debug("Trying to reset password with token: {}", token);
    Map<String, String> claims = fafTokenService.resolveToken(FafTokenType.PASSWORD_RESET, token);

    int userId = Integer.parseInt(claims.get(KEY_USER_ID));
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new ApiException(new Error(TOKEN_INVALID)));

    setPassword(user, newPassword);
  }

  private void setPassword(User user, String password) {
    log.debug("Updating FAF password for user: {}", user);
    user.setPassword(passwordEncoder.encode(password));
    userRepository.save(user);
    log.debug("Updating anope password for user: {}", user);
    anopeUserRepository.updatePassword(user.getLogin(), Hashing.md5().hashString(password, StandardCharsets.UTF_8).toString());
  }

  public User getUser(Authentication authentication) {
    if (authentication != null
      && authentication.getPrincipal() != null
      && authentication.getPrincipal() instanceof FafUserDetails) {
      return getUser(((FafUserDetails) authentication.getPrincipal()).getId());
    }
    throw new ApiException(new Error(TOKEN_INVALID));
  }

  public User getUser(int userId) {
    return userRepository.findById(userId).orElseThrow(() -> new ApiException(new Error(TOKEN_INVALID)));
  }

  public String buildSteamLinkUrl(User user, String callbackUrl) {
    log.debug("Building Steam link url for user id: {}", user.getId());
    if (user.getSteamId() != null && !Objects.equals(user.getSteamId(), "")) {
      log.debug("User with id '{}' already linked to steam", user.getId());
      throw new ApiException(new Error(ErrorCode.STEAM_ID_UNCHANGEABLE));
    }

    String token = fafTokenService.createToken(FafTokenType.LINK_TO_STEAM,
      Duration.ofHours(1),
      ImmutableMap.of(
        KEY_USER_ID, String.valueOf(user.getId()),
        KEY_STEAM_LINK_CALLBACK_URL, callbackUrl
      )
    );

    return steamService.buildLoginUrl(String.format(properties.getLinkToSteam().getSteamRedirectUrlFormat(), token));
  }

  @SneakyThrows
  public SteamLinkResult linkToSteam(String token, String steamId) {
    log.debug("linkToSteam requested for steamId '{}' with token: {}", steamId, token);
    List<Error> errors = new ArrayList<>();
    Map<String, String> attributes = fafTokenService.resolveToken(FafTokenType.LINK_TO_STEAM, token);

    User user = userRepository.findById(Integer.parseInt(attributes.get(KEY_USER_ID)))
      .orElseThrow(() -> new ApiException(new Error(TOKEN_INVALID)));

    if (!steamService.ownsForgedAlliance(steamId)) {
      errors.add(new Error(ErrorCode.STEAM_LINK_NO_FA_GAME));
    }

    Optional<User> userWithSameSteamIdOptional = userRepository.findOneBySteamId(steamId);
    userWithSameSteamIdOptional.ifPresent(userWithSameId ->
      errors.add(new Error(ErrorCode.STEAM_ID_ALREADY_LINKED, userWithSameId.getLogin()))
    );

    if (errors.isEmpty()) {
      user.setSteamId(steamId);
      userRepository.save(user);
    }

    String callbackUrl = attributes.get(KEY_STEAM_LINK_CALLBACK_URL);
    return new SteamLinkResult(callbackUrl, errors);
  }

  @Value
  static class SteamLinkResult {
    String callbackUrl;
    List<Error> errors;
  }
}

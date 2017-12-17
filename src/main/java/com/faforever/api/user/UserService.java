package com.faforever.api.user;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.NameRecord;
import com.faforever.api.data.domain.User;
import com.faforever.api.email.EmailService;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.player.PlayerRepository;
import com.faforever.api.security.FafPasswordEncoder;
import com.faforever.api.security.FafTokenService;
import com.faforever.api.security.FafTokenType;
import com.faforever.api.security.FafUserDetails;
import com.google.common.collect.ImmutableMap;
import com.google.common.hash.Hashing;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import static com.faforever.api.error.ErrorCode.TOKEN_INVALID;

@Service
@Slf4j
public class UserService {
  static final String KEY_USERNAME = "username";
  static final String KEY_EMAIL = "email";
  static final String KEY_PASSWORD = "password";
  static final String KEY_USER_ID = "id";
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

  public UserService(EmailService emailService, PlayerRepository playerRepository, UserRepository userRepository,
                     NameRecordRepository nameRecordRepository, FafApiProperties properties, AnopeUserRepository anopeUserRepository, FafTokenService fafTokenService, SteamService steamService) {
    this.emailService = emailService;
    this.playerRepository = playerRepository;
    this.userRepository = userRepository;
    this.nameRecordRepository = nameRecordRepository;
    this.properties = properties;
    this.anopeUserRepository = anopeUserRepository;
    this.fafTokenService = fafTokenService;
    this.steamService = steamService;
    this.passwordEncoder = new FafPasswordEncoder();
  }

  void register(String username, String email, String password) {
    log.debug("Registration requested for user: {}", username);
    validateUsername(username);
    emailService.validateEmailAddress(email);

    if (userRepository.existsByEmailIgnoreCase(email)) {
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
        KEY_EMAIL, email,
        KEY_PASSWORD, passwordEncoder.encode(password)
      ));

    String activationUrl = String.format(properties.getRegistration().getActivationUrlFormat(), token);

    emailService.sendActivationMail(username, email, activationUrl);
  }

  private void validateUsername(String username) {
    if (!USERNAME_PATTERN.matcher(username).matches()) {
      throw new ApiException(new Error(ErrorCode.USERNAME_INVALID, username));
    }
    if (userRepository.existsByLoginIgnoreCase(username)) {
      throw new ApiException(new Error(ErrorCode.USERNAME_TAKEN, username));
    }
  }

  /**
   * Creates a new user based on the information in the activation token.
   *
   * @param token the JWT in the format: <pre>
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
  void activate(String token) {
    Map<String, String> claims = fafTokenService.resolveToken(FafTokenType.REGISTRATION, token);

    String username = claims.get(KEY_USERNAME);
    String email = claims.get(KEY_EMAIL);
    String password = claims.get(KEY_PASSWORD);

    User user = new User();
    user.setPassword(password);
    user.setEmail(email);
    user.setLogin(username);

    log.debug("User has been activated: {}", user);
    userRepository.save(user);
  }

  void changePassword(String currentPassword, String newPassword, User user) {
    if (!Objects.equals(user.getPassword(), passwordEncoder.encode(currentPassword))) {
      throw new ApiException(new Error(ErrorCode.PASSWORD_CHANGE_FAILED_WRONG_PASSWORD));
    }

    setPassword(user, newPassword);
  }

  void changeLogin(String newLogin, User user) {
    validateUsername(newLogin);

    int minDaysBetweenChange = properties.getUser().getMinimumDaysBetweenUsernameChange();
    nameRecordRepository.getDaysSinceLastNewRecord(user.getId(), minDaysBetweenChange)
      .ifPresent(daysSinceLastRecord -> {
        throw new ApiException(new Error(ErrorCode.USERNAME_CHANGE_TOO_EARLY, minDaysBetweenChange - daysSinceLastRecord.intValue()));
      });

    int usernameReservationTimeInMonths = properties.getUser().getUsernameReservationTimeInMonths();
    nameRecordRepository.getLastUsernameOwnerWithinMonths(newLogin, usernameReservationTimeInMonths)
      .ifPresent(reservedByUserId -> {
        if (reservedByUserId != user.getId()) {
          throw new ApiException(new Error(ErrorCode.USERNAME_RESERVED, newLogin, usernameReservationTimeInMonths));
        }
      });

    log.debug("Changing username for user ''{}'' to ''{}''", user, newLogin);
    NameRecord nameRecord = new NameRecord()
      .setName(user.getLogin())
      .setPlayer(playerRepository.findOne(user.getId()));
    nameRecordRepository.save(nameRecord);

    user.setLogin(newLogin);
    userRepository.save(user);
  }

  public void changeEmail(String currentPassword, String newEmail, User user) {
    if (!Objects.equals(user.getPassword(), passwordEncoder.encode(currentPassword))) {
      throw new ApiException(new Error(ErrorCode.EMAIL_CHANGE_FAILED_WRONG_PASSWORD));
    }

    emailService.validateEmailAddress(newEmail);

    log.debug("Changing email for user ''{}'' to ''{}''", user, newEmail);
    user.setEmail(newEmail);
    userRepository.save(user);
  }

  void resetPassword(String identifier, String newPassword) {
    log.debug("Password reset requested for user-identifier: {}", identifier);

    User user = userRepository.findOneByLoginIgnoreCase(identifier)
      .orElseGet(() -> userRepository.findOneByEmailIgnoreCase(identifier)
        .orElseThrow(() -> new ApiException(new Error(ErrorCode.UNKNOWN_IDENTIFIER))));

    String token = fafTokenService.createToken(FafTokenType.PASSWORD_RESET,
      Duration.ofSeconds(properties.getRegistration().getLinkExpirationSeconds()),
      ImmutableMap.of(KEY_USER_ID, String.valueOf(user.getId()),
        KEY_PASSWORD, newPassword));

    String passwordResetUrl = String.format(properties.getPasswordReset().getPasswordResetUrlFormat(), token);

    emailService.sendPasswordResetMail(user.getLogin(), user.getEmail(), passwordResetUrl);
  }

  @SneakyThrows
  void claimPasswordResetToken(String token) {
    log.debug("Trying to reset password with token: {}", token);
    Map<String, String> claims = fafTokenService.resolveToken(FafTokenType.PASSWORD_RESET, token);

    int userId = Integer.parseInt(claims.get(KEY_USER_ID));
    String newPassword = claims.get(KEY_PASSWORD);
    User user = userRepository.findOne(userId);

    if (user == null) {
      throw new ApiException(new Error(ErrorCode.TOKEN_INVALID));
    }

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
      return userRepository.findOne(((FafUserDetails) authentication.getPrincipal()).getId());
    }
    throw new ApiException(new Error(TOKEN_INVALID));
  }

  public String buildSteamLinkUrl(User user) {
    log.debug("Building Steam link url for user id: {}", user.getId());
    if (user.getSteamId() != null && !Objects.equals(user.getSteamId(), "")) {
      log.debug("User with id '{}' already linked to steam", user.getId());
      throw new ApiException(new Error(ErrorCode.STEAM_ID_UNCHANGEABLE));
    }

    String token = fafTokenService.createToken(FafTokenType.LINK_TO_STEAM,
      Duration.ofHours(1),
      ImmutableMap.of(KEY_USER_ID, String.valueOf(user.getId()))
    );

    return steamService.buildLoginUrl(String.format(properties.getLinkToSteam().getSteamRedirectUrlFormat(), token));
  }

  @SneakyThrows
  public void linkToSteam(String token, String steamId) {
    log.debug("linkToSteam requested for steamId '{}' with token: {}", steamId, token);
    Map<String, String> attributes = fafTokenService.resolveToken(FafTokenType.LINK_TO_STEAM, token);

    User user = userRepository.findOne(Integer.parseInt(attributes.get(KEY_USER_ID)));

    if (user == null) {
      throw new ApiException(new Error(ErrorCode.TOKEN_INVALID));
    }

    if (!steamService.ownsForgedAlliance(steamId)) {
      throw new ApiException(new Error(ErrorCode.STEAM_LINK_NO_FA_GAME));
    }

    user.setSteamId(steamId);
    userRepository.save(user);
  }
}

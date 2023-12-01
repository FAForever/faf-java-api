package com.faforever.api.user;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.AccountLink;
import com.faforever.api.data.domain.LinkedServiceType;
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
import com.faforever.api.security.FafUserAuthenticationToken;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import static com.faforever.api.error.ErrorCode.TOKEN_INVALID;
import static com.faforever.api.error.ErrorCode.UNKNOWN_STEAM_ID;
import static com.github.nocatch.NoCatch.noCatch;

@Service
@Slf4j
public class UserService {
  static final String KEY_USERNAME = "username";
  static final String KEY_EMAIL = "email";
  static final String KEY_USER_ID = "id";
  static final String KEY_STEAM_CALLBACK_URL = "callbackUrl";

  private static final Pattern USERNAME_PATTERN = Pattern.compile("[A-Za-z][A-Za-z0-9_-]{2,15}$");
  private static final String USER_REGISTRATIONS_COUNT = "user.registrations.count";
  private static final String USER_PASSWORD_RESET_COUNT = "user.password.reset.count";
  private static final String STEP_TAG = "step";
  private static final String MODE_TAG = "step";

  private final EmailService emailService;
  private final PlayerRepository playerRepository;
  private final UserRepository userRepository;
  private final AccountLinkRepository accountLinkRepository;
  private final NameRecordRepository nameRecordRepository;
  private final FafApiProperties properties;
  private final FafPasswordEncoder passwordEncoder;
  private final FafTokenService fafTokenService;
  private final SteamService steamService;
  private final GogService gogService;
  private final MeterRegistry meterRegistry;
  private final Counter userRegistrationCounter;
  private final Counter userActivationCounter;
  private final Counter userSteamLinkRequestedCounter;
  private final Counter userSteamLinkDoneCounter;
  private final Counter userSteamLinkFailedCounter;
  private final Counter userNameChangeCounter;
  private final Counter userPasswordResetRequestCounter;
  private final Counter userPasswordResetViaSteamRequestCounter;
  private final Counter userPasswordResetDoneCounter;
  private final Counter userPasswordResetFailedCounter;
  private final Counter userPasswordResetDoneViaSteamCounter;
  private final Counter userPasswordResetFailedViaSteamCounter;
  private final ApplicationEventPublisher eventPublisher;

  public UserService(EmailService emailService,
                     PlayerRepository playerRepository,
                     UserRepository userRepository,
                     AccountLinkRepository accountLinkRepository,
                     NameRecordRepository nameRecordRepository,
                     FafApiProperties properties,
                     FafTokenService fafTokenService,
                     SteamService steamService,
                     GogService gogService,
                     MeterRegistry meterRegistry,
                     ApplicationEventPublisher eventPublisher) {
    this.emailService = emailService;
    this.playerRepository = playerRepository;
    this.userRepository = userRepository;
    this.accountLinkRepository = accountLinkRepository;
    this.nameRecordRepository = nameRecordRepository;
    this.properties = properties;
    this.fafTokenService = fafTokenService;
    this.steamService = steamService;
    this.gogService = gogService;
    this.eventPublisher = eventPublisher;
    this.meterRegistry = meterRegistry;

    this.passwordEncoder = new FafPasswordEncoder();

    userRegistrationCounter = meterRegistry.counter(USER_REGISTRATIONS_COUNT, STEP_TAG, "registration");
    userActivationCounter = meterRegistry.counter(USER_REGISTRATIONS_COUNT, STEP_TAG, "activation");
    userSteamLinkRequestedCounter = meterRegistry.counter(USER_REGISTRATIONS_COUNT, STEP_TAG, "steamLinkRequested");
    userSteamLinkDoneCounter = meterRegistry.counter(USER_REGISTRATIONS_COUNT, STEP_TAG, "steamLinkDone");
    userSteamLinkFailedCounter = meterRegistry.counter(USER_REGISTRATIONS_COUNT, STEP_TAG, "steamLinkFailed");
    userNameChangeCounter = meterRegistry.counter("user.name.change.count");
    userPasswordResetRequestCounter = meterRegistry.counter(USER_PASSWORD_RESET_COUNT, STEP_TAG, "request", MODE_TAG, "email");
    userPasswordResetViaSteamRequestCounter = meterRegistry.counter(USER_PASSWORD_RESET_COUNT, STEP_TAG, "request", MODE_TAG, "steam");
    userPasswordResetDoneCounter = meterRegistry.counter(USER_PASSWORD_RESET_COUNT, STEP_TAG, "done", MODE_TAG, "email");
    userPasswordResetFailedCounter = meterRegistry.counter(USER_PASSWORD_RESET_COUNT, STEP_TAG, "failed", MODE_TAG, "email");
    userPasswordResetDoneViaSteamCounter = meterRegistry.counter(USER_PASSWORD_RESET_COUNT, STEP_TAG, "done", MODE_TAG, "steam");
    userPasswordResetFailedViaSteamCounter = meterRegistry.counter(USER_PASSWORD_RESET_COUNT, STEP_TAG, "failed", MODE_TAG, "steam");
  }

  void register(String username, String email) {
    log.debug("Registration requested for user: {}", username);
    validateUsername(username);
    emailService.validateEmailAddress(email);

    if (userRepository.existsByEmail(email)) {
      throw ApiException.of(ErrorCode.EMAIL_REGISTERED, email);
    }

    int usernameReservationTimeInMonths = properties.getUser().getUsernameReservationTimeInMonths();
    nameRecordRepository.getLastUsernameOwnerWithinMonths(username, usernameReservationTimeInMonths)
      .ifPresent(reservedByUserId -> {
        throw ApiException.of(ErrorCode.USERNAME_RESERVED, username, usernameReservationTimeInMonths);
      });

    String token = fafTokenService.createToken(FafTokenType.REGISTRATION,
      Duration.ofSeconds(properties.getRegistration().getLinkExpirationSeconds()),
      Map.of(
        KEY_USERNAME, username,
        KEY_EMAIL, email
      ));

    String activationUrl = String.format(properties.getRegistration().getActivationUrlFormat(), username, token);

    // There is no recovery if the email can't be sent. The user can't continue without the mail, we need to inform the caller.
    noCatch(() -> emailService.sendActivationMail(username, email, activationUrl));

    userRegistrationCounter.increment();
  }

  private void validateUsername(String username) {
    if (!USERNAME_PATTERN.matcher(username).matches()) {
      throw ApiException.of(ErrorCode.USERNAME_INVALID, username);
    }
    if (userRepository.existsByLogin(username)) {
      throw ApiException.of(ErrorCode.USERNAME_TAKEN, username);
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
  @Transactional
  public void activate(String registrationToken, String password, String ipAddress) {
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

    log.info("User has been activated: {}", user);

    broadcastUserChange(user);
    userActivationCounter.increment();

    try {
      emailService.sendWelcomeToFafMail(username, email);
    } catch (IOException e) {
      log.error("Sending welcome email to {} failed, activation finished anyway.", username, e);
      // The welcome email is not critical, thus we swallow the exception if it fails
    }
  }

  void changePassword(String currentPassword, String newPassword, User user) {
    if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
      throw ApiException.of(ErrorCode.PASSWORD_CHANGE_FAILED_WRONG_PASSWORD);
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
    String oldLogin = user.getLogin();
    if (!newLogin.equalsIgnoreCase(oldLogin)) {
      validateUsername(newLogin);
    }

    if (!force) {
      int minDaysBetweenChange = properties.getUser().getMinimumDaysBetweenUsernameChange();
      nameRecordRepository.getDaysSinceLastNewRecord(user.getId(), minDaysBetweenChange)
        .ifPresent(daysSinceLastRecord -> {
          throw ApiException.of(ErrorCode.USERNAME_CHANGE_TOO_EARLY, minDaysBetweenChange - daysSinceLastRecord.intValue() + 1);
        });

      int usernameReservationTimeInMonths = properties.getUser().getUsernameReservationTimeInMonths();
      nameRecordRepository.getLastUsernameOwnerWithinMonths(newLogin, usernameReservationTimeInMonths)
        .ifPresent(reservedByUserId -> {
          if (!reservedByUserId.equals(user.getId())) {
            throw ApiException.of(ErrorCode.USERNAME_RESERVED, newLogin, usernameReservationTimeInMonths);
          }
        });

    }
    log.info("Changing username for user ''{}'' to ''{}'', forced:''{}''", oldLogin, newLogin, force);
    NameRecord nameRecord = new NameRecord()
      .setName(oldLogin)
      .setPlayer(playerRepository.getReferenceById(user.getId()));
    nameRecordRepository.save(nameRecord);

    user.setLogin(newLogin);
    user.setRecentIpAddress(ipAddress);

    userRepository.save(user);
    broadcastUserChange(user);
    userNameChangeCounter.increment();
  }

  public void changeEmail(String currentPassword, String newEmail, User user, String ipAddress) {
    if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
      throw ApiException.of(ErrorCode.EMAIL_CHANGE_FAILED_WRONG_PASSWORD);
    }

    emailService.validateEmailAddress(newEmail);

    log.debug("Changing email for user ''{}'' to ''{}''", user.getLogin(), newEmail);
    user.setEmail(newEmail);
    user.setRecentIpAddress(ipAddress);

    userRepository.save(user);
    broadcastUserChange(user);
  }

  public void resynchronizeAccount(User user) {
    log.debug("Resynchronizing account data for user ''{}''", user.getLogin());
    broadcastUserChange(user);
  }

  private void broadcastUserChange(User user) {
    UserUpdatedEvent userUpdatedEvent = new UserUpdatedEvent(
      user,
      user.getId(),
      user.getLogin(),
      user.getEmail(),
      user.getRecentIpAddress()
    );

    eventPublisher.publishEvent(userUpdatedEvent);
  }

  void requestPasswordReset(String identifier) {
    log.debug("Password reset requested for user-identifier: {}", identifier);

    User user = userRepository.findOneByLogin(identifier)
      .orElseGet(() -> userRepository.findOneByEmail(identifier)
        .orElseThrow(() -> ApiException.of(ErrorCode.UNKNOWN_IDENTIFIER, identifier)));

    String token = fafTokenService.createToken(FafTokenType.PASSWORD_RESET,
      Duration.ofSeconds(properties.getPasswordReset().getLinkExpirationSeconds()),
      Map.of(KEY_USER_ID, String.valueOf(user.getId())));

    String passwordResetUrl = String.format(properties.getPasswordReset().getPasswordResetUrlFormat(), user.getLogin(), token);

    // There is no recovery if the email can't be sent. The user can't continue without the mail, we need to inform the caller.
    noCatch(() -> emailService.sendPasswordResetMail(user.getLogin(), user.getEmail(), passwordResetUrl));

    userPasswordResetRequestCounter.increment();
  }

  void performPasswordReset(String token, String newPassword) {
    log.debug("Trying to reset password with token: {}", token);
    Map<String, String> claims = fafTokenService.resolveToken(FafTokenType.PASSWORD_RESET, token);

    int userId = Integer.parseInt(claims.get(KEY_USER_ID));
    User user = userRepository.findById(userId)
      .orElseThrow(() -> {
        userPasswordResetFailedCounter.increment();
        return ApiException.of(TOKEN_INVALID);
      });

    setPassword(user, newPassword);
    userPasswordResetDoneCounter.increment();
  }

  CallbackResult requestPasswordResetViaSteam(String steamId) {
    log.debug("Preparing password reset request for Steam ID: {}", steamId);

    return accountLinkRepository.findOneByServiceIdAndServiceType(steamId, LinkedServiceType.STEAM)
      .map(AccountLink::getUser)
      .map(steamUser -> {
          String token = fafTokenService.createToken(
            FafTokenType.PASSWORD_RESET,
            Duration.ofSeconds(properties.getPasswordReset().getLinkExpirationSeconds()),
            Map.of(UserService.KEY_USER_ID, String.valueOf(steamUser.getId()))
          );

          String callbackUrl = String.format(properties.getPasswordReset().getPasswordResetUrlFormat(), steamUser.getLogin(), token);
          return new CallbackResult(callbackUrl, List.of());
        }
      )
      .orElseThrow(() -> ApiException.of(UNKNOWN_STEAM_ID, steamId));
  }

  private void setPassword(User user, String password) {
    log.debug("Updating FAF password for user: {}", user.getLogin());
    user.setPassword(passwordEncoder.encode(password));
    userRepository.save(user);
  }

  public User getUser(Authentication authentication) {
    if (authentication instanceof FafUserAuthenticationToken fafUserAuthenticationToken) {
      return getUser(fafUserAuthenticationToken.getUserId());
    }
    throw ApiException.of(TOKEN_INVALID);
  }

  public User getUser(int userId) {
    return userRepository.findById(userId).orElseThrow(() -> ApiException.of(TOKEN_INVALID));
  }

  public String buildSteamLinkUrl(User user, String callbackUrl) {
    log.debug("Building Steam link url for user id: {}", user.getId());
    if (accountLinkRepository.existsByUserAndServiceType(user, LinkedServiceType.STEAM)) {
      log.debug("User with id '{}' already linked to steam", user.getId());
      throw ApiException.of(ErrorCode.STEAM_ID_UNCHANGEABLE);
    }

    String token = fafTokenService.createToken(FafTokenType.LINK_TO_STEAM,
      Duration.ofHours(6),
      Map.of(
        KEY_USER_ID, String.valueOf(user.getId()),
        KEY_STEAM_CALLBACK_URL, callbackUrl
      )
    );

    userSteamLinkRequestedCounter.increment();
    return steamService.buildLoginUrl(String.format(properties.getSteam().getLinkToSteamRedirectUrlFormat(), token));
  }

  public String buildSteamPasswordResetUrl() {
    userPasswordResetRequestCounter.increment();
    return steamService.buildLoginUrl(properties.getSteam().getSteamPasswordResetRedirectUrlFormat());
  }

  public CallbackResult linkToSteam(String token, String steamId) {
    log.debug("linkToSteam requested for steamId '{}' with token: {}", steamId, token);
    List<Error> errors = new ArrayList<>();
    Map<String, String> attributes = fafTokenService.resolveToken(FafTokenType.LINK_TO_STEAM, token);

    try {
      User user = userRepository.findById(Integer.parseInt(attributes.get(KEY_USER_ID)))
        .orElseThrow(() -> ApiException.of(TOKEN_INVALID));

      if (!steamService.ownsForgedAlliance(steamId)) {
        throw ApiException.of(ErrorCode.STEAM_LINK_NO_FA_GAME);
      }

      accountLinkRepository.findOneByServiceIdAndServiceType(steamId, LinkedServiceType.STEAM)
        .ifPresent(linkWithSameId -> {
          if (linkWithSameId.getUser() != null) {
            throw ApiException.of(ErrorCode.STEAM_ID_ALREADY_LINKED, linkWithSameId.getUser().getLogin());
          } else {
            throw ApiException.of(ErrorCode.STEAM_ID_ALREADY_LINKED);
          }
        });

      String newId = UUID.randomUUID().toString();
      while (accountLinkRepository.existsById(newId)) {
        newId = UUID.randomUUID().toString();
      }

      AccountLink accountLink = new AccountLink()
        .setId(newId)
        .setUser(user)
        .setServiceId(steamId)
        .setServiceType(LinkedServiceType.STEAM)
        .setOwnership(true)
        .setPublic(false);
      accountLinkRepository.save(accountLink);
    } catch (ApiException e) {
      errors.addAll(Arrays.asList(e.getErrors()));
    }

    if (errors.isEmpty()) {
      userSteamLinkDoneCounter.increment();
    } else {
      userSteamLinkFailedCounter.increment();
    }

    String callbackUrl = attributes.get(KEY_STEAM_CALLBACK_URL);
    return new CallbackResult(callbackUrl, errors);
  }

  @Transactional
  public void linkToGogAccount(@NotNull String gogUsername, @NotNull User user) {
    log.debug("Verifying and attempting to link user {} to gog account {}", user.getId(), gogUsername);

    if (accountLinkRepository.existsByUserAndServiceType(user, LinkedServiceType.GOG)) {
      throw ApiException.of(ErrorCode.GOG_ID_UNCHANGEABLE);
    }

    gogService.verifyGogUsername(gogUsername);
    gogService.verifyProfileToken(gogUsername, user, gogService.buildGogToken(user));
    gogService.verifyGameOwnership(gogUsername);

    accountLinkRepository.findOneByServiceIdAndServiceType(gogUsername, LinkedServiceType.GOG)
      .ifPresent(userWithSameId -> {
        if (userWithSameId.getUser() != null) {
          throw ApiException.of(ErrorCode.GOG_ID_ALREADY_LINKED, userWithSameId.getUser().getLogin());
        } else {
          throw ApiException.of(ErrorCode.GOG_ID_ALREADY_LINKED);
        }
      });

    String newId = UUID.randomUUID().toString();
    while (accountLinkRepository.existsById(newId)) {
      newId = UUID.randomUUID().toString();
    }

    AccountLink accountLink = new AccountLink()
      .setId(newId)
      .setUser(user)
      .setServiceId(gogUsername)
      .setServiceType(LinkedServiceType.GOG)
      .setOwnership(true)
      .setPublic(false);
    accountLinkRepository.save(accountLink);

    log.info("Successfully linked user {} to gog account {}", user.getId(), gogUsername);
  }

  record CallbackResult(String callbackUrl, List<Error> errors) {
  }
}

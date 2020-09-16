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
import com.faforever.api.player.PlayerRepository;
import com.faforever.api.rating.GlobalRatingRepository;
import com.faforever.api.rating.Ladder1v1RatingRepository;
import com.faforever.api.security.FafPasswordEncoder;
import com.faforever.api.security.FafTokenService;
import com.faforever.api.security.FafTokenType;
import com.faforever.api.user.UserService.CallbackResult;
import com.google.common.hash.Hashing;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.faforever.api.error.ApiExceptionMatcher.hasErrorCode;
import static com.faforever.api.user.UserService.KEY_PASSWORD;
import static com.faforever.api.user.UserService.KEY_STEAM_CALLBACK_URL;
import static com.faforever.api.user.UserService.KEY_USER_ID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
  public static final String INVALID_PASSWORD = "invalid password";
  private static final String TEST_SECRET = "banana";
  private static final int TEST_USERID = 5;
  private static final String TEST_USERNAME = "Junit";
  private static final String TEST_USERNAME_CHANGED = "newLogin";
  private static final String TEST_CURRENT_EMAIL = "junit@example.com";
  private static final String TEST_NEW_EMAIL = "junit@example.com";
  private static final String TEST_CURRENT_PASSWORD = "oldPassword";
  private static final String TEST_NEW_PASSWORD = "newPassword";
  private static final String TEST_CALLBACK_URL = "http://example.com/callback";
  private static final String TOKEN_VALUE = "someToken";
  private static final String PASSWORD_RESET_URL_FORMAT = "http://www.example.com/resetPassword/username=%s&token=%s";
  private static final String ACTIVATION_URL_FORMAT = "http://www.example.com/%s";
  private static final String STEAM_ID = "someSteamId";
  private static final String STEAM_LOGIN_URL = "steamLoginUrl";
  private static final String IP_ADDRESS = "127.0.0.1";
  private static final FafPasswordEncoder fafPasswordEncoder = new FafPasswordEncoder();
  private static final CompletableFuture<Object> COMPLETED_FUTURE = CompletableFuture.completedFuture(null);

  private UserService instance;
  @Mock
  private EmailService emailService;
  @Mock
  private UserRepository userRepository;
  @Mock
  private PlayerRepository playerRepository;
  @Mock
  private NameRecordRepository nameRecordRepository;
  @Mock
  private AnopeUserRepository anopeUserRepository;
  @Mock
  private SteamService steamService;
  @Mock
  private FafTokenService fafTokenService;
  @Mock
  private GlobalRatingRepository globalRatingRepository;
  @Mock
  private Ladder1v1RatingRepository ladder1v1RatingRepository;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private MeterRegistry meterRegistry;
  @Mock
  private ApplicationEventPublisher eventPublisher;

  private FafApiProperties properties;

  private static User createUser(int id, String name, String password, String email) {
    return (User) new User()
      .setPassword(fafPasswordEncoder.encode(password))
      .setLogin(name)
      .setEmail(email)
      .setId(id);
  }

  @BeforeEach
  public void setUp() {
    properties = new FafApiProperties();
    properties.getLinkToSteam().setSteamRedirectUrlFormat("%s");
    instance = new UserService(emailService,
      playerRepository,
      userRepository,
      nameRecordRepository,
      properties,
      anopeUserRepository,
      fafTokenService,
      steamService,
      globalRatingRepository,
      ladder1v1RatingRepository,
      meterRegistry,
      eventPublisher);
  }

  @Test
  public void register() {
    properties.getRegistration().setActivationUrlFormat(ACTIVATION_URL_FORMAT);

    when(fafTokenService.createToken(any(), any(), any())).thenReturn(TOKEN_VALUE);

    instance.register(TEST_USERNAME, TEST_CURRENT_EMAIL);

    verify(emailService).validateEmailAddress(TEST_CURRENT_EMAIL);
    verify(userRepository).existsByEmail(TEST_CURRENT_EMAIL);

    ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
    verify(emailService).sendActivationMail(eq(TEST_USERNAME), eq(TEST_CURRENT_EMAIL), urlCaptor.capture());
    assertThat(urlCaptor.getValue(), is(String.format(ACTIVATION_URL_FORMAT, TEST_USERNAME, TOKEN_VALUE)));

    verifyNoMoreInteractions(eventPublisher);
  }

  @Test
  public void registerEmailAlreadyRegistered() {
    when(userRepository.existsByEmail(TEST_CURRENT_EMAIL)).thenReturn(true);

    ApiException exception = assertThrows(ApiException.class, () -> instance.register("junit", TEST_CURRENT_EMAIL));
    assertThat(exception, hasErrorCode(ErrorCode.EMAIL_REGISTERED));
  }

  @ParameterizedTest
  @ValueSource(strings = {"junit,", "_junit", "ju"})
  public void registerInvalidUsername(String invalidUsername) {
    ApiException exception = assertThrows(ApiException.class, () -> instance.register(invalidUsername, TEST_CURRENT_EMAIL));
    assertThat(exception, hasErrorCode(ErrorCode.USERNAME_INVALID));
  }

  @Test
  public void registerUsernameTaken() {
    when(userRepository.existsByLogin("junit")).thenReturn(true);
    ApiException exception = assertThrows(ApiException.class, () -> instance.register("junit", TEST_CURRENT_EMAIL));
    assertThat(exception, hasErrorCode(ErrorCode.USERNAME_TAKEN));
  }

  @Test
  public void registerUsernameReserved() {
    when(nameRecordRepository.getLastUsernameOwnerWithinMonths(any(), anyInt())).thenReturn(Optional.of(1));

    ApiException exception = assertThrows(ApiException.class, () -> instance.register("junit", TEST_CURRENT_EMAIL));
    assertThat(exception, hasErrorCode(ErrorCode.USERNAME_RESERVED));
  }

  @Test
  public void activate() {
    final String TEST_IP_ADDRESS = IP_ADDRESS;

    when(fafTokenService.resolveToken(FafTokenType.REGISTRATION, TOKEN_VALUE)).thenReturn(Map.of(
      UserService.KEY_USERNAME, TEST_USERNAME,
      UserService.KEY_EMAIL, TEST_CURRENT_EMAIL
    ));
    when(userRepository.save(any(User.class))).then(invocation -> ((User) invocation.getArgument(0)).setId(TEST_USERID));

    instance.activate(TOKEN_VALUE, TEST_NEW_PASSWORD, TEST_IP_ADDRESS);

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    verify(globalRatingRepository).save(any(GlobalRating.class));
    verify(ladder1v1RatingRepository).save(any(Ladder1v1Rating.class));

    User user = captor.getValue();
    assertThat(user.getLogin(), is(TEST_USERNAME));
    assertThat(user.getEmail(), is(TEST_CURRENT_EMAIL));
    assertThat(user.getPassword(), is(fafPasswordEncoder.encode(TEST_NEW_PASSWORD)));
    assertThat(user.getRecentIpAddress(), is(TEST_IP_ADDRESS));

    verify(eventPublisher).publishEvent(any(UserUpdatedEvent.class));
  }

  @Test
  public void changePassword() {
    User user = createUser(TEST_USERID, TEST_USERNAME, TEST_CURRENT_PASSWORD, TEST_CURRENT_EMAIL);

    instance.changePassword(TEST_CURRENT_PASSWORD, TEST_NEW_PASSWORD, user);
    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    assertEquals(captor.getValue().getPassword(), fafPasswordEncoder.encode(TEST_NEW_PASSWORD));
    verify(anopeUserRepository).updatePassword(TEST_USERNAME, Hashing.md5().hashString(TEST_NEW_PASSWORD, StandardCharsets.UTF_8).toString());

    verifyNoMoreInteractions(eventPublisher);
  }

  @Test
  public void changePasswordInvalidPassword() {
    User user = createUser(TEST_USERID, TEST_USERNAME, INVALID_PASSWORD, TEST_CURRENT_EMAIL);

    ApiException exception = assertThrows(ApiException.class, () -> instance.changePassword(TEST_CURRENT_PASSWORD, TEST_NEW_PASSWORD, user));
    assertThat(exception, hasErrorCode(ErrorCode.PASSWORD_CHANGE_FAILED_WRONG_PASSWORD));
  }

  @Test
  public void changeEmail() {
    User user = createUser(TEST_USERID, TEST_USERNAME, TEST_CURRENT_PASSWORD, TEST_CURRENT_EMAIL);

    when(userRepository.save(any(User.class))).then(invocation -> ((User) invocation.getArgument(0)).setId(TEST_USERID));

    instance.changeEmail(TEST_CURRENT_PASSWORD, TEST_NEW_EMAIL, user, IP_ADDRESS);
    verify(emailService).validateEmailAddress(TEST_NEW_EMAIL);
    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    assertEquals(captor.getValue().getEmail(), TEST_NEW_EMAIL);

    verify(eventPublisher).publishEvent(any(UserUpdatedEvent.class));
  }

  @Test
  public void changeEmailInvalidPassword() {
    User user = createUser(TEST_USERID, TEST_USERNAME, INVALID_PASSWORD, TEST_CURRENT_EMAIL);

    ApiException exception = assertThrows(ApiException.class, () -> instance.changeEmail(TEST_CURRENT_PASSWORD, TEST_NEW_PASSWORD, user, IP_ADDRESS));
    assertThat(exception, hasErrorCode(ErrorCode.EMAIL_CHANGE_FAILED_WRONG_PASSWORD));
  }

  @Test
  public void changeLogin() {
    User user = createUser(TEST_USERID, TEST_USERNAME, TEST_CURRENT_PASSWORD, TEST_CURRENT_EMAIL);
    when(nameRecordRepository.getDaysSinceLastNewRecord(anyInt(), anyInt())).thenReturn(Optional.empty());

    when(userRepository.save(any(User.class))).then(invocation -> ((User) invocation.getArgument(0)).setId(TEST_USERID));


    instance.changeLogin(TEST_USERNAME_CHANGED, user, IP_ADDRESS);
    ArgumentCaptor<User> captorUser = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captorUser.capture());
    assertEquals(captorUser.getValue().getLogin(), TEST_USERNAME_CHANGED);
    ArgumentCaptor<NameRecord> captorNameRecord = ArgumentCaptor.forClass(NameRecord.class);
    verify(nameRecordRepository).save(captorNameRecord.capture());
    assertEquals(captorNameRecord.getValue().getName(), TEST_USERNAME);

    verify(eventPublisher).publishEvent(any(UserUpdatedEvent.class));
  }

  @Test
  public void changeLoginWithUsernameInUse() {
    User user = createUser(TEST_USERID, TEST_USERNAME, TEST_CURRENT_PASSWORD, TEST_CURRENT_EMAIL);
    when(userRepository.existsByLogin(TEST_USERNAME_CHANGED)).thenReturn(true);

    ApiException exception = assertThrows(ApiException.class, () -> instance.changeLogin(TEST_USERNAME_CHANGED, user, IP_ADDRESS));
    assertThat(exception, hasErrorCode(ErrorCode.USERNAME_TAKEN));
  }

  @Test
  public void changeLoginWithUsernameInUseButForced() {
    User user = createUser(TEST_USERID, TEST_USERNAME, TEST_CURRENT_PASSWORD, TEST_CURRENT_EMAIL);
    when(userRepository.existsByLogin(TEST_USERNAME_CHANGED)).thenReturn(true);

    ApiException exception = assertThrows(ApiException.class, () -> instance.changeLoginForced(TEST_USERNAME_CHANGED, user, IP_ADDRESS));
    assertThat(exception, hasErrorCode(ErrorCode.USERNAME_TAKEN));
  }

  @Test
  public void changeLoginWithInvalidUsername() {
    User user = createUser(TEST_USERID, TEST_USERNAME, TEST_CURRENT_PASSWORD, TEST_CURRENT_EMAIL);

    ApiException exception = assertThrows(ApiException.class, () -> instance.changeLogin("$%&", user, IP_ADDRESS));
    assertThat(exception, hasErrorCode(ErrorCode.USERNAME_INVALID));
  }

  @Test
  public void changeLoginWithInvalidUsernameButForced() {
    User user = createUser(TEST_USERID, TEST_USERNAME, TEST_CURRENT_PASSWORD, TEST_CURRENT_EMAIL);

    ApiException exception = assertThrows(ApiException.class, () -> instance.changeLoginForced("$%&", user, IP_ADDRESS));
    assertThat(exception, hasErrorCode(ErrorCode.USERNAME_INVALID));
  }

  @Test
  public void changeLoginTooEarly() {
    when(nameRecordRepository.getDaysSinceLastNewRecord(anyInt(), anyInt())).thenReturn(Optional.of(BigInteger.valueOf(5)));
    User user = createUser(TEST_USERID, TEST_USERNAME, TEST_CURRENT_PASSWORD, TEST_CURRENT_EMAIL);

    ApiException exception = assertThrows(ApiException.class, () -> instance.changeLogin(TEST_USERNAME_CHANGED, user, IP_ADDRESS));
    assertThat(exception, hasErrorCode(ErrorCode.USERNAME_CHANGE_TOO_EARLY));
  }

  @Test
  public void changeLoginTooEarlyButForce() {
    User user = createUser(TEST_USERID, TEST_USERNAME, TEST_CURRENT_PASSWORD, TEST_CURRENT_EMAIL);

    when(userRepository.save(any(User.class))).then(invocation -> ((User) invocation.getArgument(0)).setId(TEST_USERID));

    instance.changeLoginForced(TEST_USERNAME_CHANGED, user, IP_ADDRESS);
    verify(eventPublisher).publishEvent(any(UserUpdatedEvent.class));
  }

  @Test
  public void changeLoginUsernameReserved() {
    when(nameRecordRepository.getLastUsernameOwnerWithinMonths(any(), anyInt())).thenReturn(Optional.of(TEST_USERID + 1));
    User user = createUser(TEST_USERID, TEST_USERNAME, TEST_CURRENT_PASSWORD, TEST_CURRENT_EMAIL);

    ApiException exception = assertThrows(ApiException.class, () -> instance.changeLogin(TEST_USERNAME_CHANGED, user, IP_ADDRESS));
    assertThat(exception, hasErrorCode(ErrorCode.USERNAME_RESERVED));
  }

  @Test
  public void changeLoginUsernameReservedButForced() {
    when(userRepository.save(any(User.class))).then(invocation -> ((User) invocation.getArgument(0)).setId(TEST_USERID));

    User user = createUser(TEST_USERID, TEST_USERNAME, TEST_CURRENT_PASSWORD, TEST_CURRENT_EMAIL);
    instance.changeLoginForced(TEST_USERNAME_CHANGED, user, IP_ADDRESS);
    verify(eventPublisher).publishEvent(any(UserUpdatedEvent.class));
  }

  @Test
  public void changeLoginUsernameReservedBySelf() {
    when(nameRecordRepository.getLastUsernameOwnerWithinMonths(any(), anyInt())).thenReturn(Optional.of(new Integer(TEST_USERID)));
    when(userRepository.save(any(User.class))).then(invocation -> ((User) invocation.getArgument(0)).setId(TEST_USERID));

    User user = createUser(TEST_USERID, TEST_USERNAME, TEST_CURRENT_PASSWORD, TEST_CURRENT_EMAIL);
    instance.changeLogin(TEST_USERNAME_CHANGED, user, IP_ADDRESS);

    verify(eventPublisher).publishEvent(any(UserUpdatedEvent.class));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void resetPasswordByLogin() {
    properties.getPasswordReset().setPasswordResetUrlFormat(PASSWORD_RESET_URL_FORMAT);

    User user = createUser(TEST_USERID, TEST_USERNAME, TEST_CURRENT_PASSWORD, TEST_CURRENT_EMAIL);

    when(fafTokenService.createToken(any(), any(), any())).thenReturn(TOKEN_VALUE);
    when(userRepository.findOneByLogin(TEST_USERNAME)).thenReturn(Optional.of(user));

    instance.requestPasswordReset(TEST_USERNAME);

    verify(userRepository).findOneByLogin(TEST_USERNAME);

    ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
    verify(emailService).sendPasswordResetMail(eq(TEST_USERNAME), eq(TEST_CURRENT_EMAIL), urlCaptor.capture());
    assertThat(urlCaptor.getValue(), is(String.format(PASSWORD_RESET_URL_FORMAT, TEST_USERNAME, TOKEN_VALUE)));

    ArgumentCaptor<Map<String, String>> attributesMapCaptor = ArgumentCaptor.forClass(Map.class);
    verify(fafTokenService).createToken(eq(FafTokenType.PASSWORD_RESET), any(), attributesMapCaptor.capture());
    Map<String, String> tokenAttributes = attributesMapCaptor.getValue();
    assertThat(tokenAttributes.size(), is(1));
    assertThat(tokenAttributes.get(KEY_USER_ID), is(String.valueOf(TEST_USERID)));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void resetPasswordByEmail() {
    properties.getPasswordReset().setPasswordResetUrlFormat(PASSWORD_RESET_URL_FORMAT);

    User user = createUser(TEST_USERID, TEST_USERNAME, TEST_CURRENT_PASSWORD, TEST_CURRENT_EMAIL);

    when(fafTokenService.createToken(any(), any(), any())).thenReturn(TOKEN_VALUE);
    when(userRepository.findOneByEmail(TEST_CURRENT_EMAIL)).thenReturn(Optional.of(user));

    instance.requestPasswordReset(TEST_CURRENT_EMAIL);

    verify(userRepository).findOneByEmail(TEST_CURRENT_EMAIL);

    ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
    verify(emailService).sendPasswordResetMail(eq(TEST_USERNAME), eq(TEST_CURRENT_EMAIL), urlCaptor.capture());
    assertThat(urlCaptor.getValue(), is(String.format(PASSWORD_RESET_URL_FORMAT, TEST_USERNAME, TOKEN_VALUE)));

    ArgumentCaptor<Map<String, String>> attributesMapCaptor = ArgumentCaptor.forClass(Map.class);
    verify(fafTokenService).createToken(eq(FafTokenType.PASSWORD_RESET), any(), attributesMapCaptor.capture());
    Map<String, String> tokenAttributes = attributesMapCaptor.getValue();
    assertThat(tokenAttributes.size(), is(1));
    assertThat(tokenAttributes.get(KEY_USER_ID), is(String.valueOf(TEST_USERID)));
  }

  @Test
  public void resetPasswordUnknownUsernameAndEmail() {
    when(userRepository.findOneByEmail(TEST_CURRENT_EMAIL)).thenReturn(Optional.empty());

    ApiException exception = assertThrows(ApiException.class, () -> instance.requestPasswordReset(TEST_CURRENT_EMAIL));
    assertThat(exception, hasErrorCode(ErrorCode.UNKNOWN_IDENTIFIER));
  }

  @Test
  public void claimPasswordResetToken() {
    when(fafTokenService.resolveToken(FafTokenType.PASSWORD_RESET, TOKEN_VALUE))
      .thenReturn(Map.of(KEY_USER_ID, "5"));

    User user = createUser(TEST_USERID, TEST_USERNAME, TEST_CURRENT_PASSWORD, TEST_CURRENT_EMAIL);
    when(userRepository.findById(5)).thenReturn(Optional.of(user));

    instance.performPasswordReset(TOKEN_VALUE, TEST_NEW_PASSWORD);

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    assertEquals(captor.getValue().getPassword(), fafPasswordEncoder.encode(TEST_NEW_PASSWORD));
    verify(anopeUserRepository).updatePassword(TEST_USERNAME, Hashing.md5().hashString(TEST_NEW_PASSWORD, StandardCharsets.UTF_8).toString());
    verifyNoMoreInteractions(eventPublisher);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void buildSteamLinkUrl() {
    when(steamService.buildLoginUrl(any())).thenReturn(STEAM_LOGIN_URL);
    when(fafTokenService.createToken(any(), any(), any())).thenReturn(TOKEN_VALUE);

    User user = createUser(TEST_USERID, TEST_USERNAME, TEST_CURRENT_PASSWORD, TEST_CURRENT_EMAIL);
    String url = instance.buildSteamLinkUrl(user, TEST_CALLBACK_URL);

    ArgumentCaptor<String> loginUrlCaptor = ArgumentCaptor.forClass(String.class);
    verify(steamService).buildLoginUrl(loginUrlCaptor.capture());

    ArgumentCaptor<Map<String, String>> attributesCaptor = ArgumentCaptor.forClass(Map.class);

    verify(fafTokenService).createToken(
      eq(FafTokenType.LINK_TO_STEAM),
      eq(Duration.ofHours(6)),
      attributesCaptor.capture()
    );

    assertThat(url, is(STEAM_LOGIN_URL));
    assertThat(loginUrlCaptor.getValue(), is(TOKEN_VALUE));
    assertThat(attributesCaptor.getValue(), is(Map.of(
      KEY_USER_ID, String.valueOf(user.getId()),
      KEY_STEAM_CALLBACK_URL, TEST_CALLBACK_URL
    )));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void buildSteamPasswordResetUrl() {
    when(steamService.buildLoginUrl(any())).thenReturn(STEAM_LOGIN_URL);
    when(fafTokenService.createToken(any(), any(), any())).thenReturn(TOKEN_VALUE);

    String url = instance.buildSteamPasswordResetUrl(TEST_CALLBACK_URL, TEST_NEW_PASSWORD);

    ArgumentCaptor<String> loginUrlCaptor = ArgumentCaptor.forClass(String.class);
    verify(steamService).buildLoginUrl(loginUrlCaptor.capture());

    ArgumentCaptor<Map<String, String>> attributesCaptor = ArgumentCaptor.forClass(Map.class);

    verify(fafTokenService).createToken(
      eq(FafTokenType.PASSWORD_RESET_STEAM),
      eq(Duration.ofHours(6)),
      attributesCaptor.capture()
    );

    assertThat(url, is(STEAM_LOGIN_URL));
    assertThat(loginUrlCaptor.getValue(), is(TOKEN_VALUE));
    assertThat(attributesCaptor.getValue(), is(Map.of(
      KEY_STEAM_CALLBACK_URL, TEST_CALLBACK_URL,
      KEY_PASSWORD, TEST_NEW_PASSWORD
    )));
  }

  @Test
  public void buildSteamLinkUrlAlreadyLinked() {
    User user = createUser(TEST_USERID, TEST_USERNAME, TEST_CURRENT_PASSWORD, TEST_CURRENT_EMAIL);
    user.setSteamId(STEAM_ID);

    ApiException exception = assertThrows(ApiException.class, () -> instance.buildSteamLinkUrl(user, TEST_CALLBACK_URL));
    assertThat(exception, hasErrorCode(ErrorCode.STEAM_ID_UNCHANGEABLE));
  }

  @Test
  public void linkToSteam() {
    when(fafTokenService.resolveToken(FafTokenType.LINK_TO_STEAM, TOKEN_VALUE))
      .thenReturn(Map.of(
        KEY_USER_ID, "5",
        KEY_STEAM_CALLBACK_URL, TEST_CALLBACK_URL
      ));
    when(steamService.ownsForgedAlliance(any())).thenReturn(true);

    User user = createUser(TEST_USERID, TEST_USERNAME, TEST_CURRENT_PASSWORD, TEST_CURRENT_EMAIL);
    when(userRepository.findById(5)).thenReturn(Optional.of(user));
    when(userRepository.findOneBySteamId(STEAM_ID)).thenReturn(Optional.empty());

    CallbackResult result = instance.linkToSteam(TOKEN_VALUE, STEAM_ID);

    assertThat(result.getCallbackUrl(), is(TEST_CALLBACK_URL));
    assertThat(result.getErrors(), is(empty()));
    assertThat(user.getSteamId(), is(STEAM_ID));
    verifyNoMoreInteractions(eventPublisher);
  }

  @Test
  public void linkToSteamUnknownUser() {
    when(fafTokenService.resolveToken(FafTokenType.LINK_TO_STEAM, TOKEN_VALUE))
      .thenReturn(Map.of(
        KEY_USER_ID, "5",
        KEY_STEAM_CALLBACK_URL, TEST_CALLBACK_URL
      ));
    when(userRepository.findById(5)).thenReturn(Optional.empty());

    CallbackResult result = instance.linkToSteam(TOKEN_VALUE, STEAM_ID);
    assertThat(result.getCallbackUrl(), is(TEST_CALLBACK_URL));
    assertThat(result.getErrors(), contains(new Error(ErrorCode.TOKEN_INVALID)));

    verifyNoMoreInteractions(eventPublisher);
  }

  @Test
  public void linkToSteamNoGame() {
    when(fafTokenService.resolveToken(FafTokenType.LINK_TO_STEAM, TOKEN_VALUE))
      .thenReturn(Map.of(
        KEY_USER_ID, "5",
        KEY_STEAM_CALLBACK_URL, TEST_CALLBACK_URL
      ));
    when(steamService.ownsForgedAlliance(any())).thenReturn(false);

    User user = createUser(TEST_USERID, TEST_USERNAME, TEST_CURRENT_PASSWORD, TEST_CURRENT_EMAIL);
    when(userRepository.findById(5)).thenReturn(Optional.of(user));

    CallbackResult result = instance.linkToSteam(TOKEN_VALUE, STEAM_ID);

    assertThat(result.getCallbackUrl(), is(TEST_CALLBACK_URL));
    assertThat(result.getErrors(), contains(new Error(ErrorCode.STEAM_LINK_NO_FA_GAME)));
    verifyNoMoreInteractions(eventPublisher);
  }

  @Test
  public void linkToSteamAlreadyLinked() {
    when(fafTokenService.resolveToken(FafTokenType.LINK_TO_STEAM, TOKEN_VALUE))
      .thenReturn(Map.of(
        KEY_USER_ID, "6",
        KEY_STEAM_CALLBACK_URL, TEST_CALLBACK_URL
      ));
    when(steamService.ownsForgedAlliance(any())).thenReturn(true);
    User otherUser = new User();
    otherUser.setLogin("axel12");
    when(userRepository.findOneBySteamId(STEAM_ID)).thenReturn(Optional.of(otherUser));

    User user = createUser(TEST_USERID, TEST_USERNAME, TEST_CURRENT_PASSWORD, TEST_CURRENT_EMAIL);
    when(userRepository.findById(6)).thenReturn(Optional.of(user));

    CallbackResult result = instance.linkToSteam(TOKEN_VALUE, STEAM_ID);

    assertThat(result.getCallbackUrl(), is(TEST_CALLBACK_URL));
    assertThat(result.getErrors(), contains(new Error(ErrorCode.STEAM_ID_ALREADY_LINKED, otherUser.getLogin())));
    verifyNoMoreInteractions(eventPublisher);
  }

  @Test
  public void testResyncAccount() {
    User user = createUser(TEST_USERID, TEST_USERNAME, TEST_CURRENT_PASSWORD, TEST_CURRENT_EMAIL);

    instance.resynchronizeAccount(user);

    verify(eventPublisher).publishEvent(any(UserUpdatedEvent.class));
  }

  @Test
  public void testPerformPasswordResetViaSteam(
    @Mock HttpServletRequest httpServletRequest
  ) {
    when(fafTokenService.resolveToken(FafTokenType.PASSWORD_RESET_STEAM, TOKEN_VALUE))
      .thenReturn(Map.of(
        KEY_STEAM_CALLBACK_URL, TEST_CALLBACK_URL,
        KEY_PASSWORD, TEST_NEW_PASSWORD
      ));
    when(steamService.parseSteamIdFromLoginRedirect(httpServletRequest))
      .thenReturn(STEAM_ID);

    User user = createUser(TEST_USERID, TEST_USERNAME, TEST_CURRENT_PASSWORD, TEST_CURRENT_EMAIL);
    when(userRepository.findOneBySteamId(STEAM_ID)).thenReturn(Optional.of(user));

    instance.performPasswordResetViaSteam(httpServletRequest, TOKEN_VALUE);

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    assertEquals(captor.getValue().getPassword(), fafPasswordEncoder.encode(TEST_NEW_PASSWORD));
    verify(anopeUserRepository).updatePassword(TEST_USERNAME, Hashing.md5().hashString(TEST_NEW_PASSWORD, StandardCharsets.UTF_8).toString());
    verifyNoMoreInteractions(eventPublisher);
  }

  @Test
  public void testPerformPasswordResetViaSteamUnknownUser(
    @Mock HttpServletRequest httpServletRequest
  ) {
    when(fafTokenService.resolveToken(FafTokenType.PASSWORD_RESET_STEAM, TOKEN_VALUE))
      .thenReturn(Map.of(
        KEY_STEAM_CALLBACK_URL, TEST_CALLBACK_URL,
        KEY_PASSWORD, TEST_NEW_PASSWORD
      ));
    when(steamService.parseSteamIdFromLoginRedirect(httpServletRequest))
      .thenReturn(STEAM_ID);

    when(userRepository.findOneBySteamId(STEAM_ID)).thenReturn(Optional.empty());

    CallbackResult result = instance.performPasswordResetViaSteam(httpServletRequest, TOKEN_VALUE);

    assertThat(result.getCallbackUrl(), is(TEST_CALLBACK_URL));
    assertThat(result.getErrors(), contains(new Error(ErrorCode.UNKNOWN_STEAM_ID)));

    verifyNoMoreInteractions(eventPublisher);
  }

}

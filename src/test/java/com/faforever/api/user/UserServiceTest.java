package com.faforever.api.user;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.NameRecord;
import com.faforever.api.data.domain.Player;
import com.faforever.api.data.domain.User;
import com.faforever.api.email.EmailService;
import com.faforever.api.error.ApiExceptionWithCode;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.player.PlayerRepository;
import com.faforever.api.security.FafPasswordEncoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.ImmutableMap;
import com.google.common.hash.Hashing;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {
  private static final String TEST_SECRET = "banana";
  private static final int TEST_USERID = 5;
  private static final String TEST_USERNAME = "Junit";
  private static final String TEST_USERNAME_CHANED = "newLogin";
  private static final String TEST_EMAIL = "junit@example.com";
  private static final String TEST_CURRENT_PASSWORD = "oldPassword";
  private static final String TEST_NEW_PASSWORD = "newPassword";

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private UserService instance;
  private ObjectMapper objectMapper;

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

  private FafApiProperties properties;
  private static FafPasswordEncoder fafPasswordEncoder = new FafPasswordEncoder();

  private static User createUser(int id, String name, String password, String email) {
    return (User) new User()
      .setPassword(fafPasswordEncoder.encode(password))
      .setId(id)
      .setLogin(name)
      .setEmail(email);
  }

  @Before
  public void setUp() throws Exception {
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());

    properties = new FafApiProperties();
    properties.getJwt().setSecret(TEST_SECRET);
    instance = new UserService(emailService, playerRepository, userRepository, nameRecordRepository, objectMapper, properties, anopeUserRepository);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void register() throws Exception {
    properties.getRegistration().setActivationUrlFormat("http://www.example.com/%s");

    instance.register(TEST_USERNAME, TEST_EMAIL, TEST_CURRENT_PASSWORD);

    verify(userRepository).existsByEmailIgnoreCase(TEST_EMAIL);

    ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
    verify(emailService).sendActivationMail(eq(TEST_USERNAME), eq(TEST_EMAIL), urlCaptor.capture());

    String activationUrl = urlCaptor.getValue();
    assertThat(activationUrl, startsWith("http://www.example.com/"));

    String token = activationUrl.split("/")[3];
    HashMap<String, String> claims = objectMapper.readValue(JwtHelper.decode(token).getClaims(), HashMap.class);

    assertThat(claims.get(UserService.KEY_ACTION), is("activate"));
    assertThat(claims.get(UserService.KEY_USERNAME), is(TEST_USERNAME));
    assertThat(claims.get(UserService.KEY_EMAIL), is(TEST_EMAIL));
    assertThat(claims.get(UserService.KEY_PASSWORD), is(fafPasswordEncoder.encode(TEST_CURRENT_PASSWORD)));
    assertThat(Instant.parse(claims.get(UserService.KEY_EXPIRY)).isAfter(Instant.now()), is(true));
  }

  @Test
  public void registerEmailAlreadyRegistered() throws Exception {
    when(userRepository.existsByEmailIgnoreCase(TEST_EMAIL)).thenReturn(true);
    expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.EMAIL_REGISTERED));

    instance.register("junit", TEST_EMAIL, TEST_CURRENT_PASSWORD);
  }

  @Test
  public void registerInvalidUsernameWithComma() throws Exception {
    expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.USERNAME_INVALID));
    instance.register("junit,", TEST_EMAIL, TEST_CURRENT_PASSWORD);
  }

  @Test
  public void registerInvalidUsernameStartsUnderscore() throws Exception {
    expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.USERNAME_INVALID));
    instance.register("_junit", TEST_EMAIL, TEST_CURRENT_PASSWORD);
  }

  @Test
  public void registerInvalidUsernameTooShort() throws Exception {
    expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.USERNAME_INVALID));
    instance.register("ju", TEST_EMAIL, TEST_CURRENT_PASSWORD);
  }

  @Test
  public void registerUsernameTaken() throws Exception {
    when(userRepository.existsByLoginIgnoreCase("junit")).thenReturn(true);
    expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.USERNAME_TAKEN));
    instance.register("junit", TEST_EMAIL, TEST_CURRENT_PASSWORD);
  }

  @Test
  public void registerUsernameReserved() throws Exception {
    when(nameRecordRepository.getLastUsernameOwnerWithinMonths(any(), anyInt())).thenReturn(Optional.of(1));
    expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.USERNAME_RESERVED));
    instance.register("junit", TEST_EMAIL, TEST_CURRENT_PASSWORD);
  }

  @Test
  public void activate() throws Exception {
    String token = JwtHelper.encode(objectMapper.writeValueAsString(ImmutableMap.of(
      UserService.KEY_ACTION, "activate",
      UserService.KEY_USERNAME, TEST_USERNAME,
      UserService.KEY_EMAIL, TEST_EMAIL,
      UserService.KEY_EXPIRY, Instant.now().plusSeconds(3600).toString(),
      UserService.KEY_PASSWORD, fafPasswordEncoder.encode(TEST_NEW_PASSWORD)
    )), new MacSigner(TEST_SECRET)).getEncoded();

    instance.activate(token);

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());

    User user = captor.getValue();
    assertThat(user.getLogin(), is(TEST_USERNAME));
    assertThat(user.getEmail(), is(TEST_EMAIL));
    assertThat(user.getPassword(), is(fafPasswordEncoder.encode(TEST_NEW_PASSWORD)));
  }

  @Test
  public void activateTokenActionNotActivate() throws Exception {
    expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.TOKEN_INVALID));

    String token = JwtHelper.encode(objectMapper.writeValueAsString(ImmutableMap.of(
      UserService.KEY_ACTION, "foobar",
      UserService.KEY_USERNAME, TEST_USERNAME,
      UserService.KEY_EMAIL, TEST_EMAIL,
      UserService.KEY_EXPIRY, Instant.now().plusSeconds(3600).toString(),
      UserService.KEY_PASSWORD, fafPasswordEncoder.encode(TEST_NEW_PASSWORD)
    )), new MacSigner(TEST_SECRET)).getEncoded();

    instance.activate(token);
  }

  @Test
  public void activateTokenExpired() throws Exception {
    expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.TOKEN_EXPIRED));

    String token = JwtHelper.encode(objectMapper.writeValueAsString(ImmutableMap.of(
      UserService.KEY_ACTION, "activate",
      UserService.KEY_USERNAME, TEST_USERNAME,
      UserService.KEY_EMAIL, TEST_EMAIL,
      UserService.KEY_EXPIRY, Instant.now().minusSeconds(1).toString(),
      UserService.KEY_PASSWORD, fafPasswordEncoder.encode(TEST_NEW_PASSWORD)
    )), new MacSigner(TEST_SECRET)).getEncoded();

    instance.activate(token);
  }

  @Test
  public void changePassword() {
    User user = createUser(TEST_USERID, TEST_USERNAME, TEST_CURRENT_PASSWORD, TEST_EMAIL);

    instance.changePassword(TEST_CURRENT_PASSWORD, TEST_NEW_PASSWORD, user);
    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    assertEquals(captor.getValue().getPassword(), fafPasswordEncoder.encode(TEST_NEW_PASSWORD));
    verify(anopeUserRepository).updatePassword(TEST_USERNAME, Hashing.md5().hashString(TEST_NEW_PASSWORD, StandardCharsets.UTF_8).toString());
  }

  @Test
  public void changePasswordInvalidPassword() {
    expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.PASSWORD_CHANGE_FAILED_WRONG_PASSWORD));

    User user = createUser(TEST_USERID, TEST_USERNAME, "invalid password", TEST_EMAIL);
    instance.changePassword(TEST_CURRENT_PASSWORD, TEST_NEW_PASSWORD, user);
  }

  @Test
  public void changeLogin() {
    User user = createUser(TEST_USERID, TEST_USERNAME, TEST_CURRENT_PASSWORD, TEST_EMAIL);
    when(playerRepository.findOne(TEST_USERID)).thenReturn(mock(Player.class));
    when(nameRecordRepository.getDaysSinceLastNewRecord(anyInt(), anyInt())).thenReturn(Optional.empty());

    instance.changeLogin(TEST_USERNAME_CHANED, user);
    ArgumentCaptor<User> captorUser = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captorUser.capture());
    assertEquals(captorUser.getValue().getLogin(), TEST_USERNAME_CHANED);
    ArgumentCaptor<NameRecord> captorNameRecord = ArgumentCaptor.forClass(NameRecord.class);
    verify(nameRecordRepository).save(captorNameRecord.capture());
    assertEquals(captorNameRecord.getValue().getName(), TEST_USERNAME);
  }

  @Test
  public void changeLoginWithUsernameInUse() {
    expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.USERNAME_TAKEN));

    User user = createUser(TEST_USERID, TEST_USERNAME, TEST_CURRENT_PASSWORD, TEST_EMAIL);
    when(userRepository.existsByLoginIgnoreCase(TEST_USERNAME_CHANED)).thenReturn(true);
    instance.changeLogin(TEST_USERNAME_CHANED, user);
  }

  @Test
  public void changeLoginWithInvalidUsername() {
    expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.USERNAME_INVALID));

    User user = createUser(TEST_USERID, TEST_USERNAME, TEST_CURRENT_PASSWORD, TEST_EMAIL);
    instance.changeLogin("$%&", user);
  }

  @Test
  public void changeLoginTooEarly() {
    expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.USERNAME_CHANGE_TOO_EARLY));
    when(nameRecordRepository.getDaysSinceLastNewRecord(anyInt(), anyInt())).thenReturn(Optional.of(5));

    User user = createUser(TEST_USERID, TEST_USERNAME, TEST_CURRENT_PASSWORD, TEST_EMAIL);
    instance.changeLogin(TEST_USERNAME_CHANED, user);
  }

  @Test
  public void changeLoginUsernameReserved() {
    expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.USERNAME_RESERVED));
    when(nameRecordRepository.getLastUsernameOwnerWithinMonths(any(), anyInt())).thenReturn(Optional.of(TEST_USERID + 1));

    User user = createUser(TEST_USERID, TEST_USERNAME, TEST_CURRENT_PASSWORD, TEST_EMAIL);
    instance.changeLogin(TEST_USERNAME_CHANED, user);
  }

  @Test
  public void changeLoginUsernameReservedBySelf() {
    when(nameRecordRepository.getLastUsernameOwnerWithinMonths(any(), anyInt())).thenReturn(Optional.of(TEST_USERID));

    User user = createUser(TEST_USERID, TEST_USERNAME, TEST_CURRENT_PASSWORD, TEST_EMAIL);
    instance.changeLogin(TEST_USERNAME_CHANED, user);
  }

  @Test
  @SneakyThrows
  @SuppressWarnings("unchecked")
  public void resetPasswordByLogin() {
    properties.getPasswordReset().setPasswordResetUrlFormat("http://www.example.com/resetPassword/%s");

    User user = createUser(TEST_USERID, TEST_USERNAME, TEST_CURRENT_PASSWORD, TEST_EMAIL);

    when(userRepository.findOneByLoginIgnoreCase(TEST_USERNAME)).thenReturn(Optional.of(user));
    instance.resetPassword(TEST_USERNAME);

    verify(userRepository).findOneByLoginIgnoreCase(TEST_USERNAME);

    ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
    verify(emailService).sendPasswordResetMail(eq(TEST_USERNAME), eq(TEST_EMAIL), urlCaptor.capture());

    String passwordResetUrl = urlCaptor.getValue();
    assertThat(passwordResetUrl, startsWith("http://www.example.com/resetPassword/"));

    String token = passwordResetUrl.split("/")[4];
    HashMap<String, String> claims = objectMapper.readValue(JwtHelper.decode(token).getClaims(), HashMap.class);

    assertThat(claims.get(UserService.KEY_ACTION), is("reset_password"));
    assertThat(claims.get(UserService.KEY_USER_ID), is(user.getId()));
    assertThat(Instant.parse(claims.get(UserService.KEY_EXPIRY)).isAfter(Instant.now()), is(true));
  }

  @Test
  @SneakyThrows
  @SuppressWarnings("unchecked")
  public void resetPasswordByEmail() {
    properties.getPasswordReset().setPasswordResetUrlFormat("http://www.example.com/resetPassword/%s");

    User user = createUser(TEST_USERID, TEST_USERNAME, TEST_CURRENT_PASSWORD, TEST_EMAIL);

    when(userRepository.findOneByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.of(user));
    instance.resetPassword(TEST_EMAIL);

    verify(userRepository).findOneByEmailIgnoreCase(TEST_EMAIL);

    ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
    verify(emailService).sendPasswordResetMail(eq(TEST_USERNAME), eq(TEST_EMAIL), urlCaptor.capture());

    String passwordResetUrl = urlCaptor.getValue();
    assertThat(passwordResetUrl, startsWith("http://www.example.com/resetPassword/"));

    String token = passwordResetUrl.split("/")[4];
    HashMap<String, String> claims = objectMapper.readValue(JwtHelper.decode(token).getClaims(), HashMap.class);

    assertThat(claims.get(UserService.KEY_ACTION), is("reset_password"));
    assertThat(claims.get(UserService.KEY_USER_ID), is(user.getId()));
    assertThat(Instant.parse(claims.get(UserService.KEY_EXPIRY)).isAfter(Instant.now()), is(true));
  }

  @Test
  @SneakyThrows
  @SuppressWarnings("unchecked")
  public void resetPasswordUnknownUsernameAndEmail() {
    expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.UNKNOWN_IDENTIFIER));

    when(userRepository.findOneByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.empty());
    when(userRepository.findOneByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.empty());
    instance.resetPassword(TEST_EMAIL);
  }

  @Test
  @SneakyThrows
  public void claimPasswordResetToken() {
    String token = JwtHelper.encode(objectMapper.writeValueAsString(ImmutableMap.of(
      UserService.KEY_ACTION, "reset_password",
      UserService.KEY_EXPIRY, Instant.now().plusSeconds(100).toString(),
      UserService.KEY_USER_ID, String.valueOf(TEST_USERID)
    )), new MacSigner(TEST_SECRET)).getEncoded();

    User user = createUser(TEST_USERID, TEST_USERNAME, TEST_CURRENT_PASSWORD, TEST_EMAIL);

    when(userRepository.findOne(5)).thenReturn(user);

    instance.claimPasswordResetToken(token, TEST_NEW_PASSWORD);

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    assertEquals(captor.getValue().getPassword(), fafPasswordEncoder.encode(TEST_NEW_PASSWORD));
    verify(anopeUserRepository).updatePassword(TEST_USERNAME, Hashing.md5().hashString(TEST_NEW_PASSWORD, StandardCharsets.UTF_8).toString());
  }

  @Test
  @SneakyThrows
  public void claimNonPasswordResetToken() {
    expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.TOKEN_INVALID));

    String token = JwtHelper.encode(objectMapper.writeValueAsString(ImmutableMap.of(
      UserService.KEY_ACTION, "foobar",
      UserService.KEY_EXPIRY, Instant.now().plusSeconds(100).toString(),
      UserService.KEY_USER_ID, String.valueOf(TEST_USERID)
    )), new MacSigner(TEST_SECRET)).getEncoded();

    instance.claimPasswordResetToken(token, TEST_NEW_PASSWORD);
  }

  @Test
  @SneakyThrows
  public void claimPasswordResetTokenExpired() {
    expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.TOKEN_EXPIRED));

    String token = JwtHelper.encode(objectMapper.writeValueAsString(ImmutableMap.of(
      UserService.KEY_ACTION, "reset_password",
      UserService.KEY_EXPIRY, Instant.now().plusSeconds(-100).toString(),
      UserService.KEY_USER_ID, String.valueOf(TEST_USERID)
    )), new MacSigner(TEST_SECRET)).getEncoded();

    instance.claimPasswordResetToken(token, TEST_NEW_PASSWORD);
  }
}

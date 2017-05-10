package com.faforever.api.user;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.User;
import com.faforever.api.email.EmailService;
import com.faforever.api.error.ApiExceptionWithCode;
import com.faforever.api.error.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.ImmutableMap;
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

import java.time.Instant;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private UserService instance;
  private ObjectMapper objectMapper;

  @Mock
  private EmailService emailService;
  @Mock
  private UserRepository userRepository;
  private FafApiProperties properties;

  @Before
  public void setUp() throws Exception {
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());

    properties = new FafApiProperties();
    properties.getJwt().setSecret("banana");
    instance = new UserService(emailService, userRepository, objectMapper, properties);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void register() throws Exception {
    properties.getRegistration().setActivationUrlFormat("http://www.example.com/%s");

    instance.register("JUnit", "junit@example.com", "junitPassword");

    verify(userRepository).existsByEmailIgnoreCase("junit@example.com");

    ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
    verify(emailService).sendActivationMail(eq("JUnit"), eq("junit@example.com"), urlCaptor.capture());

    String activationUrl = urlCaptor.getValue();
    assertThat(activationUrl, startsWith("http://www.example.com/"));

    String token = activationUrl.split("/")[3];
    HashMap<String, String> claims = objectMapper.readValue(JwtHelper.decode(token).getClaims(), HashMap.class);

    assertThat(claims.get(UserService.KEY_ACTION), is("activate"));
    assertThat(claims.get(UserService.KEY_USERNAME), is("JUnit"));
    assertThat(claims.get(UserService.KEY_EMAIL), is("junit@example.com"));
    assertThat(claims.get(UserService.KEY_PASSWORD), is("064835f77646993a2dbda12c0acfd9961b4dfea5bb45700b1d525ace77409249"));
    assertThat(Instant.parse(claims.get(UserService.KEY_EXPIRY)).isAfter(Instant.now()), is(true));
  }

  @Test
  public void registerEmailAlreadyRegistered() throws Exception {
    when(userRepository.existsByEmailIgnoreCase("junit@example.com")).thenReturn(true);
    expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.EMAIL_REGISTERED));

    instance.register("junit", "junit@example.com", "password");
  }

  @Test
  public void registerInvalidUsernameWithComma() throws Exception {
    expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.USERNAME_INVALID));
    instance.register("junit,", "junit@example.com", "password");
  }

  @Test
  public void registerInvalidUsernameStartsUnderscore() throws Exception {
    expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.USERNAME_INVALID));
    instance.register("_junit", "junit@example.com", "password");
  }

  @Test
  public void registerInvalidUsernameTooShort() throws Exception {
    expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.USERNAME_INVALID));
    instance.register("ju", "junit@example.com", "password");
  }

  @Test
  public void registerUsernameTaken() throws Exception {
    when(userRepository.existsByLoginIgnoreCase("junit")).thenReturn(true);
    expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.USERNAME_TAKEN));
    instance.register("junit", "junit@example.com", "password");
  }

  @Test
  public void activate() throws Exception {
    String token = JwtHelper.encode(objectMapper.writeValueAsString(ImmutableMap.of(
        UserService.KEY_ACTION, "activate",
        UserService.KEY_USERNAME, "JUnit",
        UserService.KEY_EMAIL, "junit@example.com",
        UserService.KEY_EXPIRY, Instant.now().plusSeconds(3600).toString(),
        UserService.KEY_PASSWORD, "ac312ba4kej18cjasn28mva05t7h4mla1scn8934nas9c"
    )), new MacSigner("banana")).getEncoded();

    instance.activate(token);

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());

    User user = captor.getValue();
    assertThat(user.getLogin(), is("JUnit"));
    assertThat(user.getEmail(), is("junit@example.com"));
    assertThat(user.getPassword(), is("ac312ba4kej18cjasn28mva05t7h4mla1scn8934nas9c"));
  }

  @Test
  public void activateTokenActionNotActivate() throws Exception {
    expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.TOKEN_INVALID));

    String token = JwtHelper.encode(objectMapper.writeValueAsString(ImmutableMap.of(
        UserService.KEY_ACTION, "foobar",
        UserService.KEY_USERNAME, "JUnit",
        UserService.KEY_EMAIL, "junit@example.com",
        UserService.KEY_EXPIRY, Instant.now().plusSeconds(3600).toString(),
        UserService.KEY_PASSWORD, "ac312ba4kej18cjasn28mva05t7h4mla1scn8934nas9c"
    )), new MacSigner("banana")).getEncoded();

    instance.activate(token);
  }

  @Test
  public void activateTokenExpired() throws Exception {
    expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.TOKEN_EXPIRED));

    String token = JwtHelper.encode(objectMapper.writeValueAsString(ImmutableMap.of(
        UserService.KEY_ACTION, "activate",
        UserService.KEY_USERNAME, "JUnit",
        UserService.KEY_EMAIL, "junit@example.com",
        UserService.KEY_EXPIRY, Instant.now().minusSeconds(1).toString(),
        UserService.KEY_PASSWORD, "ac312ba4kej18cjasn28mva05t7h4mla1scn8934nas9c"
    )), new MacSigner("banana")).getEncoded();

    instance.activate(token);
  }

  @Test
  public void changePassword() {
    User user = dummyUser();

    instance.changePassword("newPassword", user);
    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    assertEquals(captor.getValue().getPassword(), "5c29a959abce4eda5f0e7a4e7ea53dce4fa0f0abbe8eaa63717e2fed5f193d31");
  }

  @Test
  public void changeLogin() {
    User user = dummyUser();

    instance.changeLogin("newLogin", user);
    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    assertEquals(captor.getValue().getLogin(), "newLogin");
  }

  @Test
  @SneakyThrows
  @SuppressWarnings("unchecked")
  public void resetPassword() {
    properties.getPasswordReset().setPasswordResetUrlFormat("http://www.example.com/resetPassword/%s");

    User user = dummyUser();

    when(userRepository.findOneByEmailIgnoreCase("junit@example.com")).thenReturn(user);
    instance.resetPassword("junit@example.com");

    verify(userRepository).findOneByEmailIgnoreCase("junit@example.com");

    ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
    verify(emailService).sendPasswordResetMail(eq("JUnit"), eq("junit@example.com"), urlCaptor.capture());

    String passwordResetUrl = urlCaptor.getValue();
    assertThat(passwordResetUrl, startsWith("http://www.example.com/resetPassword/"));

    String token = passwordResetUrl.split("/")[4];
    HashMap<String, String> claims = objectMapper.readValue(JwtHelper.decode(token).getClaims(), HashMap.class);

    assertThat(claims.get(UserService.KEY_ACTION), is("reset_password"));
    assertThat(claims.get(UserService.KEY_USER_ID), is(dummyUser().getId()));
    assertThat(Instant.parse(claims.get(UserService.KEY_EXPIRY)).isAfter(Instant.now()), is(true));
  }

  @Test
  @SneakyThrows
  @SuppressWarnings("unchecked")
  public void resetPasswordUnknownUser() {
    expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.USERNAME_INVALID));

    when(userRepository.findOneByEmailIgnoreCase("junit@example.com")).thenReturn(null);
    instance.resetPassword("junit@example.com");
  }

  @Test
  @SneakyThrows
  public void claimPasswordResetToken() {
    String token = JwtHelper.encode(objectMapper.writeValueAsString(ImmutableMap.of(
        UserService.KEY_ACTION, "reset_password",
        UserService.KEY_EXPIRY, Instant.now().plusSeconds(100).toString(),
        UserService.KEY_USER_ID, "5"
    )), new MacSigner("banana")).getEncoded();

    User user = dummyUser();

    when(userRepository.findOne(5)).thenReturn(user);

    instance.claimPasswordResetToken(token, "newPassword");

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    assertEquals(captor.getValue().getPassword(), "5c29a959abce4eda5f0e7a4e7ea53dce4fa0f0abbe8eaa63717e2fed5f193d31");
  }

  @Test
  @SneakyThrows
  public void claimNonPasswordResetToken() {
    expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.TOKEN_INVALID));

    String token = JwtHelper.encode(objectMapper.writeValueAsString(ImmutableMap.of(
        UserService.KEY_ACTION, "foobar",
        UserService.KEY_EXPIRY, Instant.now().plusSeconds(100).toString(),
        UserService.KEY_USER_ID, "5"
    )), new MacSigner("banana")).getEncoded();

    instance.claimPasswordResetToken(token, "newPassword");
  }

  @Test
  @SneakyThrows
  public void claimPasswordResetTokenExpired() {
    expectedException.expect(ApiExceptionWithCode.apiExceptionWithCode(ErrorCode.TOKEN_EXPIRED));

    String token = JwtHelper.encode(objectMapper.writeValueAsString(ImmutableMap.of(
        UserService.KEY_ACTION, "reset_password",
        UserService.KEY_EXPIRY, Instant.now().plusSeconds(-100).toString(),
        UserService.KEY_USER_ID, "5"
    )), new MacSigner("banana")).getEncoded();

    instance.claimPasswordResetToken(token, "newPassword");
  }


  private User dummyUser() {
    User user = new User();
    user.setPassword("junitPassword");
    user.setEmail("junit@example.com");
    user.setLogin("JUnit");
    return user;
  }
}

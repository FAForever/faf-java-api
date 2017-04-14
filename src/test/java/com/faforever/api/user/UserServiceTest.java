package com.faforever.api.user;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.User;
import com.faforever.api.email.EmailService;
import com.faforever.api.error.ApiExceptionWithCode;
import com.faforever.api.error.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.ImmutableMap;
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
}

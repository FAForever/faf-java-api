package com.faforever.api.security;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.error.ApiExceptionMatcher;
import com.faforever.api.error.ErrorCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.ImmutableMap;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.migrationsupport.rules.ExpectedExceptionSupport;
import org.junit.rules.ExpectedException;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@ExtendWith(ExpectedExceptionSupport.class)
public class FafTokenServiceTest {

  private static final String TEST_SECRET = "banana";
  private final MacSigner macSigner;
  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  private ObjectMapper objectMapper;
  private FafTokenService instance;

  public FafTokenServiceTest() {
    this.macSigner = new MacSigner(TEST_SECRET);
  }

  @BeforeEach
  public void setUp() {
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());

    FafApiProperties properties = new FafApiProperties();
    properties.getJwt().setSecret(TEST_SECRET);

    instance = new FafTokenService(objectMapper, properties);
  }

  @Test
  public void createToken() throws Exception {
    String token = instance.createToken(FafTokenType.REGISTRATION, Duration.ofSeconds(100), Collections.emptyMap());

    Jwt jwt = JwtHelper.decodeAndVerify(token, macSigner);
    Map<String, String> claims = objectMapper.readValue(jwt.getClaims(), new TypeReference<Map<String, String>>() {
    });

    assertThat(claims.get(FafTokenService.KEY_ACTION), is(FafTokenType.REGISTRATION.toString()));
    assertTrue(claims.containsKey(FafTokenService.KEY_LIFETIME));
    assertThat(claims.size(), is(2));
  }

  @Test
  public void createTokenWithAttributes() throws Exception {
    Map<String, String> attributes = ImmutableMap.of("attribute1", "value1", "attribute2", "value2");
    String token = instance.createToken(FafTokenType.REGISTRATION, Duration.ofSeconds(100), attributes);

    Jwt jwt = JwtHelper.decodeAndVerify(token, macSigner);
    Map<String, String> claims = objectMapper.readValue(jwt.getClaims(), new TypeReference<Map<String, String>>() {
    });


    assertThat(claims.get(FafTokenService.KEY_ACTION), is(FafTokenType.REGISTRATION.toString()));
    assertTrue(claims.containsKey(FafTokenService.KEY_LIFETIME));
    assertThat(claims.get("attribute1"), is("value1"));
    assertThat(claims.get("attribute2"), is("value2"));
    assertThat(claims.size(), is(4));
  }

  @Test
  public void resolveToken() throws Exception {
    String token = instance.createToken(FafTokenType.REGISTRATION, Duration.ofSeconds(100), Collections.emptyMap());
    Map<String, String> result = instance.resolveToken(FafTokenType.REGISTRATION, token);

    assertThat(result.size(), is(0));
  }

  @Test
  public void resolveTokenWithAttributes() throws Exception {
    Map<String, String> attributes = ImmutableMap.of("attribute1", "value1", "attribute2", "value2");

    String token = instance.createToken(FafTokenType.REGISTRATION, Duration.ofSeconds(100), attributes);
    Map<String, String> result = instance.resolveToken(FafTokenType.REGISTRATION, token);

    assertThat(result.size(), is(2));
    assertThat(result.get("attribute1"), is("value1"));
    assertThat(result.get("attribute2"), is("value2"));
  }

  @Test
  public void resolveTokenExpired() throws Exception {
    expectedException.expect(ApiExceptionMatcher.hasErrorCode(ErrorCode.TOKEN_EXPIRED));

    String token = instance.createToken(FafTokenType.REGISTRATION, Duration.ofSeconds(-1), Collections.emptyMap());
    instance.resolveToken(FafTokenType.REGISTRATION, token);
  }

  @Test
  public void resolveTokenInvalidType() throws Exception {
    expectedException.expect(ApiExceptionMatcher.hasErrorCode(ErrorCode.TOKEN_INVALID));

    String token = instance.createToken(FafTokenType.REGISTRATION, Duration.ofSeconds(-1), Collections.emptyMap());
    instance.resolveToken(FafTokenType.PASSWORD_RESET, token);
  }

  @Test
  public void resolveGibberishToken() throws Exception {
    expectedException.expect(ApiExceptionMatcher.hasErrorCode(ErrorCode.TOKEN_INVALID));
    instance.resolveToken(FafTokenType.PASSWORD_RESET, "gibberish token");
  }
}

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
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@ExtendWith(ExpectedExceptionSupport.class)
public class FafTokenServiceTest {
  private static final String TEST_SECRET_KEY =
    "-----BEGIN RSA PRIVATE KEY-----\n" +
      "MIIEpgIBAAKCAQEAzTpJ/ytBu3dih41bEqRnchNfkyCigIWDHrbBa+0Szw853Oew\n" +
      "LJ1Zwe5wh9L8nxWD7f+pVDg5dAKXAJabmx6vuXhJjekIxrHTIRdxebO2norhAK/Q\n" +
      "OuOZ4NKinShpWwyLgmPcJZaKq1TNtVm7No/3CQK5XugDJ5NG2c1RNEAdk3pk2EkB\n" +
      "lhhObJW2vUY+KmJ+4ndwZKeyJD4CTr4sJK7cWRf2Ht2RWq6omFlsnQiPGyO9/VYo\n" +
      "YQz3fVTiXdf4+xg6toBaVvult0YPS5/jm611RrsMwLM4ZpWkUlytxR/N8/oLb9rD\n" +
      "nO9+OQZDpIsKfNd3ZAqg/wZTHIcb474UHjUTiQIDAQABAoIBAQCMuO1IZNbbvs72\n" +
      "97x9GfI8zH/6mKQU0HfKNbKHWLZO+LfKe6vXy8ViLydGWywRwWUHawkm0K7El4oH\n" +
      "Qz5LrUz9NjfpcOMtq32D8VlEBDCyobQLDoMP/kTjXktWzAECB6YZsHOh6ooHVU0A\n" +
      "jxjKHwlbSlzlcN3I4znv2tNVqqkdF9Gbg7wUmN9n0qpj+7kDtkixJy3jm9YLxKCS\n" +
      "pNZ1UUjGKtVgl/1871slNUtANHj/xCnkYrOncrIXf472pEeSxBU5JlI4fILcyTtG\n" +
      "B9btuYBk7Z239TWDEZTqIyst0QGteNRsjE+gkB9WV1ra9JPPWDiBYye4qqaIs3al\n" +
      "jd3lkMApAoGBAP+i1aJ/c8XV18eTMYLmQZRnkjrkxyQMhJ/x+6tow6p6A16lDwHh\n" +
      "tRoyQk0XdTpQegu+YtdBXSRk6zNzE2njWEVOMK4/Zqt5a1yMSE/8MMQVler1ChdF\n" +
      "PWhZCPb+CfKm1RHXpFMsmBZx+7MumLQwjCtZfQl8YMt34gfVcXRSZ50DAoGBAM2F\n" +
      "FVrTTUVaGv6zjdO7K+5eUj0VRR2nGId5nIWqouQryJaizeBZfWatjDYVbl0qHFy2\n" +
      "QnHA+3UEsVWOkJG90rZcP4UWcDy86e5T/3FR2Xfy3kW10Gfe6hrjjbjYflleD5Qg\n" +
      "uZ9ovk/TZjTjvMWisNBSW1FILz9SMLWHoCFPGOmDAoGBANKRb4X1lAiOt7n13d+k\n" +
      "CLrUgVgvoHVqNkiFi7dKiXnAHUx1i6ISKBoW8hQMUYyiQ5Wu0j3a4n0a/74WeRRM\n" +
      "pyYXXPP613hBgJTwHJR9+DFcUmwCQbifWRC93iuNX+ZXU8Tpqrq0TeaXJywWIsSy\n" +
      "BJOkl+EbaaPP8Qhg4Z5eTmi/AoGBAMzSDyA/acjuLe0cwQH8jaG3+rnJkuIkf3u0\n" +
      "pVtJXaGMSRJnGkq2pRVJbG0SGrVanH2BXuLDc1eB38HmnQnCZlc7xEo8vIqrs2/D\n" +
      "4tXqvpKeRwquUg7Sx/kYQ0uu5uzloxz7KENIPjKL+lZHiQBmTVSwXzW4fO3cWZLw\n" +
      "oZPQooFFAoGBAMVvhsOmlpwyyS9s/CVIMirvLQEuEIIS5fMcnmCmu8P49ZZ7YSVf\n" +
      "2OJOSGj+lWkEMf2qOW7kYl303GrESeJ36KmLbDthnH+p6RSq5NzN5CAucffA0tsa\n" +
      "keX0a6YHVu9doUPhUFJdbgg8FIL1FVEJROQckMAiDcYg2mFXmtaVTjqX\n" +
      "-----END RSA PRIVATE KEY-----";
  private static final String TEST_PUBLIC_KEY = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDNOkn/K0G7d2KHjVsSpGdyE1+TIKKAhYMetsFr7RLPDznc57AsnVnB7nCH0vyfFYPt/6lUODl0ApcAlpubHq+5eEmN6QjGsdMhF3F5s7aeiuEAr9A645ng0qKdKGlbDIuCY9wlloqrVM21Wbs2j/cJArle6AMnk0bZzVE0QB2TemTYSQGWGE5slba9Rj4qYn7id3Bkp7IkPgJOviwkrtxZF/Ye3ZFarqiYWWydCI8bI739VihhDPd9VOJd1/j7GDq2gFpW+6W3Rg9Ln+ObrXVGuwzAszhmlaRSXK3FH83z+gtv2sOc7345BkOkiwp813dkCqD/BlMchxvjvhQeNROJ api@faforever.com";
  private final RsaSigner rsaSigner;
  private final RsaVerifier rsaVerifier;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  private ObjectMapper objectMapper;
  private FafTokenService instance;

  public FafTokenServiceTest() {
    this.rsaSigner = new RsaSigner(TEST_SECRET_KEY);
    this.rsaVerifier = new RsaVerifier(TEST_PUBLIC_KEY);
  }

  @BeforeEach
  public void setUp() {
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());

    FafApiProperties properties = new FafApiProperties();
    properties.getJwt().setSecretKey(TEST_SECRET_KEY);
    properties.getJwt().setPublicKey(TEST_PUBLIC_KEY);

    instance = new FafTokenService(objectMapper, properties);
  }

  @Test
  public void createToken() throws Exception {
    String token = instance.createToken(FafTokenType.REGISTRATION, Duration.ofSeconds(100), Collections.emptyMap());

    Jwt jwt = JwtHelper.decodeAndVerify(token, rsaVerifier);
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

    Jwt jwt = JwtHelper.decodeAndVerify(token, rsaVerifier);
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

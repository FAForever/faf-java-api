package com.faforever.api.security;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class FafTokenService {
  static final String KEY_ACTION = "action";
  static final String KEY_LIFETIME = "lifetime";

  private final ObjectMapper objectMapper;
  private final FafApiProperties properties;
  private final MacSigner macSigner;

  public FafTokenService(ObjectMapper objectMapper, FafApiProperties properties) {
    this.objectMapper = objectMapper;
    this.properties = properties;
    this.macSigner = new MacSigner(properties.getJwt().getSecret());
  }

  /**
   * Creates a signed token with a map of attributes and time-limited validity
   *
   * @see #resolveToken(FafTokenType, String)
   */
  @SneakyThrows
  public String createToken(@NotNull FafTokenType type, @NotNull TemporalAmount lifetime, @NotNull Map<String, String> attributes) {
    Assert.notNull(attributes, "Attributes map must not be null");
    Assert.isTrue(!attributes.containsKey(KEY_ACTION), MessageFormat.format("`{0}` is a protected attributed and must not be used", KEY_ACTION));
    Assert.isTrue(!attributes.containsKey(KEY_LIFETIME), MessageFormat.format("`{0}` is a protected attributed and must not be used", KEY_LIFETIME));

    HashMap<String, String> claims = new HashMap<>(attributes);
    claims.put(KEY_ACTION, type.toString());
    Instant expiresAt = Instant.now().plus(lifetime);
    claims.put(KEY_LIFETIME, expiresAt.toString());

    log.debug("Creating token of type `{0}` expiring at `{1}` with attributes: {2}", type, expiresAt, attributes);

    return JwtHelper.encode(objectMapper.writeValueAsString(claims), macSigner).getEncoded();
  }

  /**
   * Verifies a token regarding it's type, lifetime and signature
   *
   * @return Map of original attributes
   * @see #createToken(FafTokenType, TemporalAmount, Map)
   */
  public Map<String, String> resolveToken(@NotNull FafTokenType expectedTokenType, @NotNull String token) {
    Map<String, String> claims = null;

    try {
      claims = objectMapper.readValue(JwtHelper.decodeAndVerify(token, macSigner).getClaims(), new TypeReference<Map<String, String>>() {
      });
      Assert.notNull(claims, "claims must not be null");
      Assert.isTrue(claims.containsKey(KEY_ACTION), "Token does not contain: " + KEY_ACTION);
      Assert.isTrue(claims.containsKey(KEY_LIFETIME), "Token does not contain: " + KEY_LIFETIME);
      FafTokenType actualTokenType = FafTokenType.valueOf(claims.get(KEY_ACTION));
      Assert.state(expectedTokenType == actualTokenType, String.format("Token types do not match, expected: %s, actual: %s", expectedTokenType, actualTokenType));
    } catch (IOException | IllegalArgumentException | IllegalStateException e) {
      if (claims == null) {
        log.warn("Unparseable token of expected type {}: {}", expectedTokenType, token);
      } else {
        log.warn("Token of expected type `{}` invalid: {}", expectedTokenType, token);
      }

      throw new ApiException(new Error(ErrorCode.TOKEN_INVALID));
    }

    Instant expiresAt = Instant.parse(claims.get(KEY_LIFETIME));

    if (expiresAt.isBefore(Instant.now())) {
      log.debug("Token of expected type `{}` invalid: {}", expectedTokenType, token);
      throw new ApiException(new Error(ErrorCode.TOKEN_EXPIRED));
    }

    HashMap<String, String> attributes = new HashMap<>(claims);
    attributes.remove(KEY_ACTION);
    attributes.remove(KEY_LIFETIME);

    return attributes;
  }

}

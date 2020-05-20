package com.faforever.api.security;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.file.Files;
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
  private final RsaSigner rsaSigner;
  private final RsaVerifier rsaVerifier;

  public FafTokenService(ObjectMapper objectMapper, FafApiProperties properties) throws IOException {
    String secretKey = Files.readString(properties.getJwt().getSecretKeyPath());
    String publicKey = Files.readString(properties.getJwt().getPublicKeyPath());

    this.objectMapper = objectMapper;
    this.rsaSigner = new RsaSigner(secretKey);
    this.rsaVerifier = new RsaVerifier(publicKey);
  }

  /**
   * Creates a signed token with a map of attributes and time-limited validity.
   *
   * @see #resolveToken(FafTokenType, String)
   */
  @SneakyThrows
  public String createToken(@NotNull FafTokenType type, @NotNull TemporalAmount lifetime, @NotNull Map<String, String> attributes) {
    Assert.notNull(attributes, "Attributes map must not be null");
    Assert.isTrue(!attributes.containsKey(KEY_ACTION), MessageFormat.format("'{0}' is a protected attributed and must not be used", KEY_ACTION));
    Assert.isTrue(!attributes.containsKey(KEY_LIFETIME), MessageFormat.format("'{0}' is a protected attributed and must not be used", KEY_LIFETIME));

    Map<String, String> claims = new HashMap<>(attributes);
    claims.put(KEY_ACTION, type.toString());
    Instant expiresAt = Instant.now().plus(lifetime);
    claims.put(KEY_LIFETIME, expiresAt.toString());

    log.debug("Creating token of type '{}' expiring at '{}' with attributes: {}", type, expiresAt, attributes);

    return JwtHelper.encode(objectMapper.writeValueAsString(claims), rsaSigner).getEncoded();
  }

  /**
   * Verifies a token regarding its type, lifetime and signature.
   *
   * @return Map of original attributes
   * @see #createToken(FafTokenType, TemporalAmount, Map)
   */
  @SneakyThrows
  public Map<String, String> resolveToken(@NotNull FafTokenType expectedTokenType, @NotNull String token) {
    Map<String, String> claims;

    try {
      claims = objectMapper.readValue(JwtHelper.decodeAndVerify(token, rsaVerifier).getClaims(), new TypeReference<Map<String, String>>() {
      });
    } catch (JsonProcessingException | IllegalArgumentException e) {
      log.warn("Unparseable token: {}", token);
      throw new ApiException(new Error(ErrorCode.TOKEN_INVALID));
    }

    if (!claims.containsKey(KEY_ACTION)) {
      log.warn("Missing key '{}' in token: {}", KEY_ACTION, token);
      throw new ApiException(new Error(ErrorCode.TOKEN_INVALID));
    }

    if (!claims.containsKey(KEY_LIFETIME)) {
      log.warn("Missing key '{}' in token: {}", KEY_LIFETIME, token);
      throw new ApiException(new Error(ErrorCode.TOKEN_INVALID));
    }

    FafTokenType actualTokenType;
    try {
      actualTokenType = FafTokenType.valueOf(claims.get(KEY_ACTION));
    } catch (IllegalArgumentException e) {
      log.warn("Unknown FAF token type '{}' in token: {}", claims.get(KEY_ACTION), token);
      throw new ApiException(new Error(ErrorCode.TOKEN_INVALID));
    }

    if (expectedTokenType != actualTokenType) {
      log.warn("Token types do not match (expected: '{}', actual: '{}') for token: {}", expectedTokenType, actualTokenType, token);
      throw new ApiException(new Error(ErrorCode.TOKEN_INVALID));
    }

    Instant expiresAt = Instant.parse(claims.get(KEY_LIFETIME));
    if (expiresAt.isBefore(Instant.now())) {
      log.debug("Token already expired at '{}' for token: {}", expiresAt, token);
      throw new ApiException(new Error(ErrorCode.TOKEN_EXPIRED));
    }

    Map<String, String> attributes = new HashMap<>(claims);
    attributes.remove(KEY_ACTION);
    attributes.remove(KEY_LIFETIME);

    return attributes;
  }

}

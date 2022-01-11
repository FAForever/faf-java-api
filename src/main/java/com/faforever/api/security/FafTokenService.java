package com.faforever.api.security;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.security.crypto.RsaKeyHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.validation.constraints.NotNull;
import java.nio.file.Files;
import java.security.interfaces.RSAPublicKey;
import java.text.MessageFormat;
import java.text.ParseException;
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
  private final RSASSASigner rsaSigner;
  private final RSASSAVerifier rsaVerifier;

  public FafTokenService(ObjectMapper objectMapper, FafApiProperties properties) throws Exception {
    String secretKey = Files.readString(properties.getJwt().getSecretKeyPath());
    String publicKey = Files.readString(properties.getJwt().getPublicKeyPath());

    RSAKey parsedSecretKey = (RSAKey) RSAKey.parseFromPEMEncodedObjects(secretKey);
    RSAPublicKey parsedPublicKey = RsaKeyHelper.parsePublicKey(publicKey.trim());

    this.rsaSigner = new RSASSASigner(parsedSecretKey);
    this.rsaVerifier = new RSASSAVerifier(parsedPublicKey);
    this.objectMapper = objectMapper;
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

    return signJwt(claims);
  }

  private String signJwt(Object data) throws Exception {
    JWSObject jwsObject = new JWSObject(
      new JWSHeader.Builder(JWSAlgorithm.RS256)
        .type(JOSEObjectType.JWT)
        .build(),
      new Payload(objectMapper.writeValueAsString(data))
    );

    jwsObject.sign(rsaSigner);

    return jwsObject.serialize();
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
      JWSObject jwsObject = JWSObject.parse(token);
      if (!jwsObject.verify(rsaVerifier)) {
        throw ApiException.of(ErrorCode.TOKEN_INVALID);
      }
      String payload = jwsObject.getPayload().toString();

      claims = objectMapper.readValue(payload, new TypeReference<>() {
      });
    } catch (ParseException | JOSEException | JsonProcessingException | IllegalArgumentException e) {
      log.warn("Unparseable token: {}", token);
      throw ApiException.of(ErrorCode.TOKEN_INVALID);
    }

    if (!claims.containsKey(KEY_ACTION)) {
      log.warn("Missing key '{}' in token: {}", KEY_ACTION, token);
      throw ApiException.of(ErrorCode.TOKEN_INVALID);
    }

    if (!claims.containsKey(KEY_LIFETIME)) {
      log.warn("Missing key '{}' in token: {}", KEY_LIFETIME, token);
      throw ApiException.of(ErrorCode.TOKEN_INVALID);
    }

    FafTokenType actualTokenType;
    try {
      actualTokenType = FafTokenType.valueOf(claims.get(KEY_ACTION));
    } catch (IllegalArgumentException e) {
      log.warn("Unknown FAF token type '{}' in token: {}", claims.get(KEY_ACTION), token);
      throw ApiException.of(ErrorCode.TOKEN_INVALID);
    }

    if (expectedTokenType != actualTokenType) {
      log.warn("Token types do not match (expected: '{}', actual: '{}') for token: {}", expectedTokenType, actualTokenType, token);
      throw ApiException.of(ErrorCode.TOKEN_INVALID);
    }

    Instant expiresAt = Instant.parse(claims.get(KEY_LIFETIME));
    if (expiresAt.isBefore(Instant.now())) {
      log.debug("Token already expired at '{}' for token: {}", expiresAt, token);
      throw ApiException.of(ErrorCode.TOKEN_EXPIRED);
    }

    Map<String, String> attributes = new HashMap<>(claims);
    attributes.remove(KEY_ACTION);
    attributes.remove(KEY_LIFETIME);

    return attributes;
  }

}

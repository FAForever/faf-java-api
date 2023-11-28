package com.faforever.api.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FafAuthenticationConverterTest {
  @Test
  void jwtWithUsernameShouldBeConvertedToUserToken() {
    Jwt jwt = new Jwt(
      "abc",
      null,
      null,
      Map.of(
        "alg", "RS256",
        "kid", "public:hydra.jwt.access-token",
        "typ", "JWT"
      ),
      Map.of(
        "sub", "123",
        "scp", List.of(),
        "ext", Map.of(
          "username", "fafuser"
        )
      )
    );
    AbstractAuthenticationToken converted = new FafAuthenticationConverter().convert(jwt);
    assertTrue(converted instanceof FafUserAuthenticationToken);
  }
  @Test
  void jwtWithoutUsernameShouldBeConvertedToServiceToken() {
    Jwt jwt = new Jwt(
      "abc",
      null,
      null,
      Map.of(
        "alg", "RS256",
        "kid", "public:hydra.jwt.access-token",
        "typ", "JWT"
      ),
      Map.of(
        "sub", "service",
        "scp", List.of(),
        "ext", Map.of()
      )
    );
    AbstractAuthenticationToken converted = new FafAuthenticationConverter().convert(jwt);
    assertTrue(converted instanceof FafServiceAuthenticationToken);
  }
}

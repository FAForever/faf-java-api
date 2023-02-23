package com.faforever.api.security;

import java.util.List;
import java.util.Map;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Jwt converter that reads scopes + custom FAF roles from the token extension.
 */
public class FafAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  @Override
  public AbstractAuthenticationToken convert(Jwt source) {
    int userId = extractUserId(source);
    String username = extractUsername(source);
    List<FafScope> scopes = extractScopes(source);
    List<FafRole> roles = extractRoles(source);

    return new FafAuthenticationToken(userId, username, scopes, roles);
  }

  private int extractUserId(Jwt source) {
    return Integer.parseInt(source.getSubject());
  }

  private String extractUsername(Jwt source) {
    Map<String, Object> ext = source.getClaim("ext");
    String username = (String) ext.getOrDefault("username", "[undefined]");
    return username;
  }

  private List<FafScope> extractScopes(Jwt source) {
    List<String> jwtScopes = source.getClaim("scp");
    List<FafScope> scopes = jwtScopes.stream().map(FafScope::new).toList();
    return scopes;
  }

  public List<FafRole> extractRoles(Jwt source) {
    Map<String, Object> ext = source.getClaim("ext");
    final List<FafRole> roles = ((List<String>) ext.getOrDefault("roles", List.of())).stream().map(FafRole::new).toList();
    return roles;
  }
}

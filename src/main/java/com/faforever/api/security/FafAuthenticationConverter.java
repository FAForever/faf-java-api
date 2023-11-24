package com.faforever.api.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

/**
 * Jwt converter that reads scopes + custom FAF roles from the token extension.
 */
public class FafAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  @Override
  public AbstractAuthenticationToken convert(Jwt source) {
    List<FafScope> scopes = extractScopes(source);
    List<FafRole> roles = extractRoles(source);

    String subject = extractSubject(source);

    try {
      int userId = Integer.parseInt(subject);
      String username = extractUsername(source);
      return new FafUserAuthenticationToken(userId, username, scopes, roles);
    } catch (NumberFormatException e) {
      return new FafServiceAuthenticationToken(subject, scopes);
    }
  }

  private String extractSubject(Jwt source) {
    return source.getSubject();
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

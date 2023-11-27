package com.faforever.api.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Jwt converter that reads scopes + custom FAF roles from the token extension.
 */
public class FafAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  @Override
  public AbstractAuthenticationToken convert(Jwt source) {
    List<FafScope> scopes = extractScopes(source);
    List<FafRole> roles = extractRoles(source);

    String subject = extractSubject(source);
    return extractUsername(source)
      .<FafAuthenticationToken>map(username -> new FafUserAuthenticationToken(Integer.parseInt(subject), username, scopes, roles))
      .orElseGet(() -> new FafServiceAuthenticationToken(subject, scopes));
  }

  private String extractSubject(Jwt source) {
    return source.getSubject();
  }

  private Optional<String> extractUsername(Jwt source) {
    Map<String, Object> ext = source.getClaim("ext");
    return Optional.ofNullable((String) ext.get("username"));
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

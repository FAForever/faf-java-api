package com.faforever.api.security;

import com.nimbusds.jose.shaded.gson.JsonArray;
import com.nimbusds.jose.shaded.gson.JsonObject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
    JsonObject ext = source.getClaim("ext");
    String username = Optional.ofNullable(ext)
      .flatMap(jsonObject -> Optional.ofNullable(jsonObject.get("username").getAsString()))
      .orElse("[undefined]");

    return username;
  }

  private List<FafScope> extractScopes(Jwt source) {
    JsonArray jwtScopes = source.getClaim("scp");
    List<FafScope> scopes = Optional.ofNullable(jwtScopes)
      .map(jsonArray -> StreamSupport.stream(jsonArray.spliterator(), false).map(scope -> new FafScope(scope.getAsString())))
      .orElse(Stream.empty())
      .toList();

    return scopes;
  }

  public List<FafRole> extractRoles(Jwt source) {
    JsonObject ext = source.getClaim("ext");
    List<FafRole> roles = Optional.ofNullable(ext)
      .flatMap(jsonObject -> Optional.ofNullable((JsonArray) jsonObject.get("roles")))
      .map(jsonArray -> StreamSupport.stream(jsonArray.spliterator(), false).map(role -> new FafRole(role.getAsString())))
      .orElse(Stream.empty())
      .toList();

    return roles;
  }
}

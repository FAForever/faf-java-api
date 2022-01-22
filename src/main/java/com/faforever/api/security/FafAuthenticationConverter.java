package com.faforever.api.security;

import com.nimbusds.jose.shaded.json.JSONArray;
import com.nimbusds.jose.shaded.json.JSONObject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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
    JSONObject ext = source.getClaim("ext");
    String username = Optional.ofNullable(ext)
      .flatMap(jsonObject -> Optional.ofNullable((String)jsonObject.get("username")))
      .orElse("[undefined]");

    return username;
  }

  private List<FafScope> extractScopes(Jwt source) {
    JSONArray jwtScopes = source.getClaim("scp");
    List<FafScope> scopes = Optional.ofNullable(jwtScopes)
      .map(jsonArray -> jsonArray.stream().map(scope -> new FafScope(scope.toString())))
      .orElse(Stream.empty())
      .toList();

    return scopes;
  }

  public List<FafRole> extractRoles(Jwt source) {
    JSONObject ext = source.getClaim("ext");
    List<FafRole> roles = Optional.ofNullable(ext)
      .flatMap(jsonObject -> Optional.ofNullable((JSONArray)jsonObject.get("roles")))
      .map(jsonArray -> jsonArray.stream().map(role -> new FafRole(role.toString())))
      .orElse(Stream.empty())
      .toList();

    return roles;
  }
}

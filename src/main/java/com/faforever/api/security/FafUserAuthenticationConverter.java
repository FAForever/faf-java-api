package com.faforever.api.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Converts a {@link FafUserDetails} from and to an {@link Authentication} for use in a JWT token.
 */
@Slf4j
public class FafUserAuthenticationConverter extends DefaultUserAuthenticationConverter {

  public static final String USER_ID_KEY = "user_id";
  public static final String NON_LOCKED = "non_locked";

  @Override
  public Map<String, ?> convertUserAuthentication(Authentication authentication) {
    FafUserDetails fafUserDetails = (FafUserDetails) authentication.getPrincipal();

    @SuppressWarnings("unchecked")
    Map<String, Object> response = (Map<String, Object>) super.convertUserAuthentication(authentication);
    response.put(USER_ID_KEY, fafUserDetails.getId());
    response.put(NON_LOCKED, fafUserDetails.isAccountNonLocked());

    return response;
  }

  @Override
  public Authentication extractAuthentication(Map<String, ?> map) {
    if (map.containsKey(USER_ID_KEY)) {
      log.debug("Access token is FAF legacy token");

      int id = (Integer) map.get(USER_ID_KEY);
      String username = (String) map.get(USERNAME);
      boolean accountNonLocked = Optional.ofNullable((Boolean) map.get(NON_LOCKED)).orElse(true);
      Collection<? extends GrantedAuthority> authorities = getAuthorities(map);
      UserDetails user = new FafUserDetails(id, username, "N/A", accountNonLocked, authorities);

      return new UsernamePasswordAuthenticationToken(user, "N/A", authorities);
    } else {
      Object sub = map.get("sub");

      if (sub == null) {
        log.debug("Access token has no user associated");
        return null;
      }

      log.debug("Access token is FAF OpenID Connect token");
      int id = Integer.parseInt((String) sub);
      var ext = (Map<String, Object>) map.get("ext");
      var roles = (List<String>) ext.get("roles");

      var authorities = roles.stream()
        .map(role -> (GrantedAuthority) () -> "ROLE_" + role)
        .toList();

      UserDetails user = new FafUserDetails(id, "username", "N/A", false, authorities);
      return new UsernamePasswordAuthenticationToken(user, "N/A", authorities);
    }

  }
}

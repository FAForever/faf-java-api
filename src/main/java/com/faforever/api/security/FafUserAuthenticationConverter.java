package com.faforever.api.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Converts a {@link FafUserDetails} from and to an {@link Authentication} for use in a JWT token.
 */
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
    if (!map.containsKey(USER_ID_KEY)) {
      return null;
    }

    int id = (Integer) map.get(USER_ID_KEY);
    String username = (String) map.get(USERNAME);
    boolean accountNonLocked = Optional.ofNullable((Boolean) map.get(NON_LOCKED)).orElse(true);
    Collection<? extends GrantedAuthority> authorities = getAuthorities(map);
    UserDetails user = new FafUserDetails(id, username, "N/A", accountNonLocked, authorities);

    return new UsernamePasswordAuthenticationToken(user, "N/A", authorities);
  }
}

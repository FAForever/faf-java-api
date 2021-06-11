package com.faforever.api.security.elide.permission;

import com.faforever.api.security.ElideUser;
import com.faforever.api.security.FafUserDetails;
import com.faforever.api.security.OAuthScope;
import com.yahoo.elide.core.security.User;
import com.yahoo.elide.core.security.checks.UserCheck;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
abstract class FafUserCheck extends UserCheck {
  protected boolean checkOAuthScopes(OAuthScope... scope) {
    return checkOAuthScopes(Stream.of(scope).map(OAuthScope::getKey).toArray(String[]::new));
  }

  private boolean checkOAuthScopes(String... scope) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!(authentication instanceof OAuth2Authentication)) {
      log.trace("Authentication is no OAuth2Authentication: {}", authentication);
      return false;
    }

    OAuth2Authentication oAuth2Authentication = ((OAuth2Authentication) authentication);
    OAuth2Request oAuth2Request = oAuth2Authentication.getOAuth2Request();
    var requestScopes = ((FafUserDetails) oAuth2Authentication.getPrincipal()).getScopes();

    List<String> missedScopes = new ArrayList<>();

    for (String currentScope : scope) {
      if (!requestScopes.contains(currentScope)) {
        missedScopes.add(currentScope);
      }
    }

    if (log.isTraceEnabled()) {
      if (missedScopes.isEmpty()) {
        log.trace("All requested scopes are granted: {}", String.join(", ", scope));
      } else {
        log.trace("Scopes '{}' are not granted in requested scopes: {} for client with id {}", String.join(", ", missedScopes), String.join(", ", oAuth2Request.getScope()), oAuth2Request.getResourceIds());
      }
    }

    return missedScopes.isEmpty();
  }

  protected boolean checkUserPermission(User user, String... userPermissionRole) {
    if (!(user instanceof ElideUser) || ((ElideUser) user).getFafUserDetails().isEmpty()) {
      log.warn("UserCheck applied on wrong User type: {}", user.getPrincipal());
      return false;
    }

    FafUserDetails userDetails = ((ElideUser) user).getFafUserDetails().get();

    Set<String> grantedUserRoles = userDetails.getAuthorities().stream()
      .map(GrantedAuthority::getAuthority)
      .collect(Collectors.toSet());

    Set<String> missedScopes = Stream.of(userPermissionRole)
      .collect(Collectors.toSet());
    missedScopes.removeAll(grantedUserRoles);

    if (log.isTraceEnabled()) {
      if (missedScopes.isEmpty()) {
        log.trace("All requested permissions are granted: {}", String.join(", ", userPermissionRole));
      } else {
        log.debug("Permissions '{}' are not granted in requested permissions: {} , for user with id: {}", String.join(", ", missedScopes), String.join(", ", grantedUserRoles), userDetails.getId());
      }
    }

    return missedScopes.isEmpty();
  }
}

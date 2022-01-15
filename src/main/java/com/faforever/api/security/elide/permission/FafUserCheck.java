package com.faforever.api.security.elide.permission;

import com.faforever.api.security.ElideUser;
import com.faforever.api.security.FafAuthenticationToken;
import com.faforever.api.security.FafRole;
import com.faforever.api.security.OAuthScope;
import com.yahoo.elide.core.security.User;
import com.yahoo.elide.core.security.checks.UserCheck;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
abstract class FafUserCheck extends UserCheck {
  protected boolean checkOAuthScopes(OAuthScope... scopes) {
    return checkOAuthScopes(Stream.of(scopes).map(OAuthScope::getKey).toArray(String[]::new));
  }

  private boolean checkOAuthScopes(String... scopes) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if(authentication instanceof FafAuthenticationToken fafAuthenticationToken) {

      List<String> missedScopes = new ArrayList<>();
      Set<String> requestedScopes = new HashSet<>(Arrays.asList(scopes));

      for (String currentScope : requestedScopes) {
        if (!fafAuthenticationToken.hasScope(currentScope)) {
          missedScopes.add(currentScope);
        }
      }

      if (log.isTraceEnabled()) {
        if (missedScopes.isEmpty()) {
          log.trace("All requested scopes are granted: {}", String.join(", ", scopes));
        } else {
          log.trace("Scopes '{}' are not granted in requested scopes: {} for client", String.join(", ", missedScopes), String.join(", ", requestedScopes));
        }
      }

      return missedScopes.isEmpty();
    } else {
      log.warn("Authentication is no FafAuthenticationToken: {}", authentication);
      return false;
    }
  }

  protected boolean checkUserPermission(User abstractUser, String... userPermissionRoles) {
    if(abstractUser instanceof ElideUser user) {
      Set<String> grantedUserRoles = user.getFafAuthentication().getRoles().stream()
        .map(FafRole::role)
        .collect(Collectors.toSet());

      Set<String> missedRoles = Stream.of(userPermissionRoles)
        .collect(Collectors.toSet());
      missedRoles.removeAll(grantedUserRoles);

      if (log.isTraceEnabled()) {
        if (missedRoles.isEmpty()) {
          log.trace("All requested permissions are granted: {}", String.join(", ", userPermissionRoles));
        } else {
          log.debug("Permissions '{}' are not granted in requested permissions: {} , for user with id: {}", String.join(", ", missedRoles), String.join(", ", grantedUserRoles), user.getFafId());
        }
      }

      return missedRoles.isEmpty();
    } else {
      log.warn("UserCheck applied on wrong User type: {}", abstractUser);
      return false;
    }
  }
}

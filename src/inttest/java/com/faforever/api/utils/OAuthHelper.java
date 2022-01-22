package com.faforever.api.utils;

import com.faforever.api.data.domain.Player;
import com.faforever.api.player.PlayerRepository;
import com.faforever.api.security.FafAuthenticationToken;
import com.faforever.api.security.FafRole;
import com.faforever.api.security.FafScope;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

@Component
public class OAuthHelper {
  private final PlayerRepository playerRepository;

  public OAuthHelper(PlayerRepository playerRepository) {
    this.playerRepository = playerRepository;
  }

  public RequestPostProcessor addBearerTokenForUser(int userId, @NotNull Set<String> scopes) {
    Player user = playerRepository.getById(userId);

    Set<FafRole> roles = user.getUserGroups().stream()
      .flatMap(userGroup -> userGroup.getPermissions().stream())
      .map(permission -> new FafRole(permission.getTechnicalName()))
      .collect(Collectors.toSet());

    var fafScopes = scopes.stream().map(FafScope::new).toList();

    return authentication(new FafAuthenticationToken(userId, user.getLogin(), fafScopes, roles));
  }

  public RequestPostProcessor addBearerToken(
    Integer userId,
    @NotNull Set<String> scopes,
    @NotNull Set<String> roles
  ) {
    var fafScopes = scopes.stream().map(FafScope::new).toList();
    var fafRoles = roles.stream().map(FafRole::new).toList();

    return authentication(new FafAuthenticationToken(userId, "[undefined]", fafScopes, fafRoles));
  }
}

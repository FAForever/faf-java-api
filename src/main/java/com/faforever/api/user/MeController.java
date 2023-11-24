package com.faforever.api.user;

import com.faforever.api.data.domain.Player;
import com.faforever.api.data.domain.UserGroup;
import com.faforever.api.player.PlayerService;
import com.faforever.api.security.FafUserAuthenticationToken;
import com.faforever.api.security.UserSupplier;
import com.yahoo.elide.jsonapi.models.Data;
import com.yahoo.elide.jsonapi.models.JsonApiDocument;
import com.yahoo.elide.jsonapi.models.Resource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides the route {@code /me} which returns the currently logged in user's information.
 */
@RestController
@RequiredArgsConstructor
public class MeController {
  private final PlayerService playerService;
  private final UserSupplier userSupplier;

  @RequestMapping(method = RequestMethod.GET, value = "/me")
  @Operation(summary = "Returns the authentication object of the current user")
  @ApiResponse(responseCode = "200", description = "Success with JsonApi compliant MeResult")
  @Secured("ROLE_USER")
  public JsonApiDocument me() {
    return userSupplier.get()
      .filter(o -> FafUserAuthenticationToken.class.isAssignableFrom(o.getClass()))
      .map(FafUserAuthenticationToken.class::cast)
      .map(authentication -> {
          Player player = playerService.getById(authentication.getUserId());
          Set<String> grantedAuthorities = authentication.getRoles().stream()
            //.map(FafRole::role)
            // TEMPORARY WORKAROUND: we stripped away the ROLE_ prefix, but clients need to adapt. Until then, we add both
            .flatMap(role -> Stream.of(role.role(), role.getAuthority()))
            .collect(Collectors.toSet());

          Set<String> groups = player.getUserGroups().stream()
            .map(UserGroup::getTechnicalName)
            .collect(Collectors.toSet());

          return new JsonApiDocument(new Data<>(
            new Resource("me",
              "me",
              null,
              Map.of(
                "userId", player.getId(),
                "userName", player.getLogin(),
                "email", player.getEmail(),
                "clan", player.getClan() == null ? Optional.empty() : Clan.builder()
                  .id(player.getClan().getId())
                  .membershipId(player.getClanMembership().getId())
                  .tag(player.getClan().getTag())
                  .name(player.getClan().getName())
                  .build(),
                "lastLogin", Optional.ofNullable(player.getLastLogin()),
                "groups", groups,
                "permissions", grantedAuthorities
              ),
              null,
              null,
              null
            )
          ));
        }
      ).orElseGet(JsonApiDocument::new);
  }

  @Value
  @Builder
  public static class Clan {
    Integer id;
    Integer membershipId;
    String tag;
    String name;
  }
}

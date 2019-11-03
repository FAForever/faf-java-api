package com.faforever.api.user;

import com.faforever.api.data.domain.Player;
import com.faforever.api.data.domain.UserGroup;
import com.faforever.api.player.PlayerService;
import com.faforever.api.security.FafUserDetails;
import com.faforever.api.web.JsonApiSingleResource;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import lombok.Builder;
import lombok.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Provides the route {@code /me} which returns the currently logged in user's information.
 */
@RestController
public class MeController {
  private final PlayerService playerService;

  public MeController(PlayerService playerService) {
    this.playerService = playerService;
  }

  @RequestMapping(method = RequestMethod.GET, value = "/me")
  @ApiOperation("Returns the authentication object of the current user")
  @ApiResponse(code = 200, message = "Success with JsonApi compliant MeResult")
  @Secured("ROLE_USER")
  public JsonApiSingleResource<MeResult> me(@AuthenticationPrincipal FafUserDetails authentication) {

    Player player = playerService.getById(authentication.getId());
    Set<String> grantedAuthorities = authentication.getAuthorities().stream()
      .map(GrantedAuthority::getAuthority)
      .collect(Collectors.toSet());

    Set<String> groups = player.getUserGroups().stream()
      .map(UserGroup::getTechnicalName)
      .collect(Collectors.toSet());

    return MeResult.builder()
      .userId(player.getId())
      .userName(player.getLogin())
      .email(player.getEmail())
      .clan(player.getClan() == null ? null : Clan.builder()
        .id(player.getClan().getId())
        .membershipId(player.getClanMembership().getId())
        .tag(player.getClan().getTag())
        .name(player.getClan().getName())
        .build()
      )
      .groups(groups)
      .permissions(grantedAuthorities)
      .build()
      .asJsonApi();
  }

  @Value
  @Builder
  public static class MeResult {
    Integer userId;
    String userName;
    String email;
    Clan clan;
    Set<String> groups;
    Set<String> permissions;

    @SuppressWarnings("unchecked")
    JsonApiSingleResource<MeResult> asJsonApi() {
      return JsonApiSingleResource.ofProxy(
        () -> "me",
        () -> "me",
        () -> this
      );
    }
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

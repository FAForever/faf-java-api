package com.faforever.api.clan;

import com.faforever.api.clan.result.ClanResult;
import com.faforever.api.clan.result.MeResult;
import com.faforever.api.clan.result.PlayerResult;
import com.faforever.api.data.domain.Clan;
import com.faforever.api.data.domain.Player;
import com.faforever.api.player.PlayerService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequestMapping(path = ClansController.PATH)
@RequiredArgsConstructor
public class ClansController {

  static final String PATH = "/clans";
  private final ClanService clanService;
  private final PlayerService playerService;

  @ApiOperation("Grab data about yourself and the clan")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Success with JSON { player: {id: ?, login: ?}, clan: { id: ?, name: ?, tag: ?}}"),
    @ApiResponse(code = 400, message = "Bad Request")})
  @GetMapping(path = "/me", produces = APPLICATION_JSON_VALUE)
  @Deprecated // use regular /me route instead
  public MeResult me() {
    Player player = playerService.getCurrentPlayer();

    Clan clan = player.getClan();
    ClanResult clanResult = null;
    if (clan != null) {
      clanResult = ClanResult.of(clan);
    }
    return new MeResult(PlayerResult.of(player), clanResult);
  }

  // This request cannot be handled by JSON API because we must simultaneously create two resources (a,b)
  // a: the new clan with the leader membership, b: the leader membership with the new clan
  @ApiOperation("Create a clan with correct leader, founder and clan membership")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Success with JSON { id: ?, type: 'clan'}"),
    @ApiResponse(code = 400, message = "Bad Request")})
  @PostMapping(path = "/create", produces = APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('ROLE_USER')")
  @Deprecated // use POST /data/clans instead (with a founder in relationships)
  public Map<String, Serializable> createClan(@RequestParam(value = "name") String name,
                                              @RequestParam(value = "tag") String tag,
                                              @RequestParam(value = "description", required = false) String description) {
    Clan clan = clanService.create(name, tag, description);
    return Map.of("id", clan.getId(), "type", "clan");
  }

  @ApiOperation("Generate invitation link")
  @ApiResponses(value = {
    @ApiResponse(code = 200, message = "Success with JSON { jwtToken: ? }"),
    @ApiResponse(code = 400, message = "Bad Request")})
  @GetMapping(path = "/generateInvitationLink", produces = APPLICATION_JSON_VALUE)
  public Map<String, Serializable> generateInvitationLink(
    @RequestParam(value = "clanId") int clanId,
    @RequestParam(value = "playerId") int newMemberId) {
    String jwtToken = clanService.generatePlayerInvitationToken(newMemberId, clanId);
    return Map.of("jwtToken", jwtToken);
  }

  @ApiOperation("Check invitation link and add member to Clan")
  @PostMapping(path = "/joinClan", produces = APPLICATION_JSON_VALUE)
  public void joinClan(@RequestParam(value = "token") String stringToken) {
    clanService.acceptPlayerInvitationToken(stringToken);
  }
}

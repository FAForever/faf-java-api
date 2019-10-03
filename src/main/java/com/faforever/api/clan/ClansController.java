package com.faforever.api.clan;

import com.faforever.api.clan.result.ClanResult;
import com.faforever.api.clan.result.MeResult;
import com.faforever.api.clan.result.PlayerResult;
import com.faforever.api.data.domain.Clan;
import com.faforever.api.data.domain.Player;
import com.faforever.api.player.PlayerService;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;


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
  @RequestMapping(path = "/me", method = RequestMethod.GET, produces = APPLICATION_JSON_UTF8_VALUE)
  public MeResult me(Authentication authentication) {
    Player player = playerService.getPlayer(authentication);

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
  @RequestMapping(path = "/create", method = RequestMethod.POST, produces = APPLICATION_JSON_UTF8_VALUE)
  @PreAuthorize("hasRole('ROLE_USER')")
  @Transactional
  public Map<String, Serializable> createClan(@RequestParam(value = "name") String name,
                                              @RequestParam(value = "tag") String tag,
                                              @RequestParam(value = "description", required = false) String description,
                                              Authentication authentication) throws IOException {
    Player player = playerService.getPlayer(authentication);
    Clan clan = clanService.create(name, tag, description, player);
    return ImmutableMap.of("id", clan.getId(), "type", "clan");
  }

  @ApiOperation("Generate invitation link")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Success with JSON { jwtToken: ? }"),
      @ApiResponse(code = 400, message = "Bad Request")})
  @RequestMapping(path = "/generateInvitationLink",
      method = RequestMethod.GET,
    produces = APPLICATION_JSON_UTF8_VALUE)
  public Map<String, Serializable> generateInvitationLink(
      @RequestParam(value = "clanId") int clanId,
      @RequestParam(value = "playerId") int newMemberId,
      Authentication authentication) throws IOException {
    Player player = playerService.getPlayer(authentication);
    String jwtToken = clanService.generatePlayerInvitationToken(player, newMemberId, clanId);
    return ImmutableMap.of("jwtToken", jwtToken);
  }

  @ApiOperation("Check invitation link and add Member to Clan")
  @RequestMapping(path = "/joinClan",
      method = RequestMethod.POST,
    produces = APPLICATION_JSON_UTF8_VALUE)
  @Transactional
  public void joinClan(
      @RequestParam(value = "token") String stringToken,
      Authentication authentication) throws IOException {
    clanService.acceptPlayerInvitationToken(stringToken, authentication);
  }
}

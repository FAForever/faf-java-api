package com.faforever.api.clan;

import com.faforever.api.data.domain.Clan;
import com.faforever.api.data.domain.ClanMembership;
import com.faforever.api.data.domain.Player;
import com.faforever.api.player.PlayerRepository;
import com.faforever.api.utils.AuthenticationHelper;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;


@RestController
@RequestMapping(path = "/clans")
public class ClansController {

  private final ClanRepository clanRepository;
  private final ClanMembershipRepository clanMembershipRepository;
  private final PlayerRepository playerRepository;

  @Inject
  public ClansController(ClanRepository clanRepository,
                         ClanMembershipRepository clanMembershipRepository,
                         PlayerRepository playerRepository) {
    this.clanRepository = clanRepository;
    this.clanMembershipRepository = clanMembershipRepository;
    this.playerRepository = playerRepository;
  }

  @ApiOperation("Create a clan with correct leader, founder and clan membership")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Success with JSON { id: <id>, type: clan}"),
      @ApiResponse(code = 400, message = "Bad Request")})
  @RequestMapping(path = "/create", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Transactional
  public Map<String, Serializable> createClan(@RequestParam(value = "name") String name,
                                              @RequestParam(value = "tag") String tag,
                                              @RequestParam(value = "description", required = false) String description,
                                              Authentication authentication) throws IOException, ClanException {
    Clan clan = new Clan();
    clan.setName(name);
    clan.setTag(tag);
    clan.setDescription(description);

    Player player = AuthenticationHelper.getPlayer(authentication, playerRepository);
    if (player.getClanMemberships().size() > 0) {
      throw new ClanException("Player is already member of a clan");
    }

    clan.setFounder(player);
    clan.setLeader(player);
    clanRepository.save(clan);

    ClanMembership membership = new ClanMembership();
    membership.setClan(clan);
    membership.setPlayer(player);
    clanMembershipRepository.save(membership);

    return ImmutableMap.of("id", clan.getId(), "type", "clan");
  }

  @ApiOperation("Grab data about yourself and the clan")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Success"),
      @ApiResponse(code = 400, message = "Bad Request")})
  @RequestMapping(path = "/me", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  @Transactional
  public Map<String, Serializable> me(Authentication authentication) {
    Player player = AuthenticationHelper.getPlayer(authentication, playerRepository);

    ImmutableMap<String, Serializable> playerMap = ImmutableMap.of(
        "id", player.getId(),
        "login", player.getLogin());

    Clan clan = (player.getClanMemberships().size() > 0)
        ? player.getClanMemberships().get(0).getClan()
        : null;
    ImmutableMap<String, Serializable> clanMap = ImmutableMap.of();
    if (clan != null) {
      clanMap = ImmutableMap.of(
          "id", clan.getId(),
          "name", clan.getName(),
          "tag", clan.getTag());
    }
    return ImmutableMap.of("player", playerMap, "clan", clanMap);
  }

  @ApiOperation("Kick a member from the clan, Delete the Clan Membership")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Success"),
      @ApiResponse(code = 400, message = "Bad Request")})
  @RequestMapping(path = "/kick", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Transactional
  public void createClan(@RequestParam(value = "membershipId") int membershipId,
                         Authentication authentication) throws IOException, ClanException {
    Player player = AuthenticationHelper.getPlayer(authentication, playerRepository);
    ClanMembership membership = clanMembershipRepository.findOne(membershipId);
    if (membership == null) {
      throw new ClanException("Clan Membership not found");
    }
    if (membership.getClan().getLeader().getId() != player.getId()) {
      throw new ClanException("Player is not the leader of the clan");
    }
    clanMembershipRepository.delete(membership);
  }

  @ExceptionHandler(ClanException.class)
  public void handleClanException(ClanException ex, HttpServletResponse response) throws IOException {
    response.sendError(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
  }
}

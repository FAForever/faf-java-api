package com.faforever.api.clan;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.Clan;
import com.faforever.api.data.domain.ClanMembership;
import com.faforever.api.data.domain.Player;
import com.faforever.api.player.PlayerRepository;
import com.faforever.api.utils.AuthenticationHelper;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;
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
@RequestMapping(path = ClansController.PATH)
public class ClansController {

  static final String PATH = "/clans";
  private final ClanRepository clanRepository;
  private final ClanMembershipRepository clanMembershipRepository;
  private final PlayerRepository playerRepository;
  private final FafApiProperties fafApiProperties;
  private final MacSigner macSigner;
  private final ObjectMapper objectMapper;
  private final ClanService clanService;

  @Inject
  public ClansController(ClanRepository clanRepository,
                         ClanMembershipRepository clanMembershipRepository,
                         PlayerRepository playerRepository, FafApiProperties fafApiProperties, ClanService clanService) {
    this.clanRepository = clanRepository;
    this.clanMembershipRepository = clanMembershipRepository;
    this.playerRepository = playerRepository;
    this.fafApiProperties = fafApiProperties;
    this.macSigner = new MacSigner(fafApiProperties.getJwtSecret());
    this.clanService = clanService;
    this.objectMapper = new ObjectMapper();
  }

  @ApiOperation("Grab data about yourself and the clan")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Success with JSON { player: {id: ?, login: ?}, clan: { id: ?, name: ?, tag: ?}}"),
      @ApiResponse(code = 400, message = "Bad Request")})
  @RequestMapping(path = "/me", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public Map<String, Serializable> me(Authentication authentication) {
    Player player = AuthenticationHelper.getPlayer(authentication, playerRepository);

    ImmutableMap<String, Serializable> playerMap = ImmutableMap.of(
        "id", player.getId(),
        "login", player.getLogin());

    Clan clan = (player.getClanMemberships().size() > 0)
        ? player.getClanMemberships().get(0).getClan()
        : null;
    Serializable clanData = "";
    if (clan != null) {
      clanData = ImmutableMap.of(
          "id", clan.getId(),
          "name", clan.getName(),
          "tag", clan.getTag());
    }
    return ImmutableMap.of("player", playerMap, "clan", clanData);
  }

  // This request cannot be handled by JSON API because we must simultaneously create two resources (a,b)
  // a: the new clan with the leader membership, b: the leader membership with the new clan
  @ApiOperation("Create a clan with correct leader, founder and clan membership")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Success with JSON { id: ?, type: 'clan'}"),
      @ApiResponse(code = 400, message = "Bad Request")})
  @RequestMapping(path = "/create", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Transactional
  public Map<String, Serializable> createClan(@RequestParam(value = "name") String name,
                                              @RequestParam(value = "tag") String tag,
                                              @RequestParam(value = "description", required = false) String description,
                                              Authentication authentication) throws IOException, ClanException {
    Player player = AuthenticationHelper.getPlayer(authentication, playerRepository);
    Clan clan = clanService.create(name, tag, description, player);
    return ImmutableMap.of("id", clan.getId(), "type", "clan");
  }

  @ApiOperation("Generate invitation link")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Success with JSON { jwtToken: ? }"),
      @ApiResponse(code = 400, message = "Bad Request")})
  @RequestMapping(path = "/generateInvitationLink",
      method = RequestMethod.GET,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Map<String, Serializable> generateInvitationLink(
      @RequestParam(value = "clanId") int clanId,
      @RequestParam(value = "playerId") int newMemberId,
      Authentication authentication) throws IOException, ClanException {

    Player player = AuthenticationHelper.getPlayer(authentication, playerRepository);
    Clan clan = clanRepository.findOne(clanId);

    checkAgainstNull(clan, "Clan");
    checkLeader(player, clan);

    Player newMember = playerRepository.getOne(newMemberId);
    checkAgainstNull(newMember, "new Clan Member");
    checkLeader(player, clan);
    // TODO: not sure if this is a good idea, e.g. Time Zones
    long expire = System.currentTimeMillis()
        + (fafApiProperties.getClan().getExpireDurationInMinutes() * 60 * 1000);

    String jwtToken = sign(
        ImmutableMap.of("newMemberId", newMemberId,
            "expire", expire,
            "clan", ImmutableMap.of("id", clan.getId(),
                "tag", clan.getTag(),
                "name", clan.getName())));
    return ImmutableMap.of("jwtToken", jwtToken);
  }

  @ApiOperation("Check invitation link and add Member to Clan")
  @RequestMapping(path = "/joinClan",
      method = RequestMethod.POST,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @Transactional
  public void joinClan(
      @RequestParam(value = "token") String stringToken,
      Authentication authentication) throws IOException, ClanException {

    Jwt token = JwtHelper.decode(stringToken);
    token.verifySignature(this.macSigner);
    JsonNode data = objectMapper.readTree(token.getClaims());

    if (data.get("expire").asLong() < System.currentTimeMillis()) {
      throw new ClanException("Invitation Link expired");  // TODO: outsource to I18n?
    }

    Player player = AuthenticationHelper.getPlayer(authentication, playerRepository);
    Clan clan = clanRepository.findOne(data.get("clan").get("id").asInt());

    checkAgainstNull(clan, "Clan");
    checkLeader(player, clan);

    Player newMember = playerRepository.findOne(data.get("newMemberId").asInt());
    checkAgainstNull(newMember, "new Player");

    if (player.getId() != newMember.getId()) {
      throw new ClanException("You cannot accept the invitation link");  // TODO: outsource to I18n?
    }
    if (newMember.getClan() != null) {
      throw new ClanException("Player is already in a Clan");  // TODO: outsource to I18n?
    }

    ClanMembership membership = new ClanMembership();
    membership.setClan(clan);
    membership.setPlayer(newMember);
    clanMembershipRepository.save(membership);
  }

  private String sign(Map<String, Serializable> data) throws IOException {
    Jwt token = JwtHelper.encode(objectMapper.writeValueAsString(data), this.macSigner);
    return token.getEncoded();
  }

  private void checkAgainstNull(Object object, String Type) throws ClanException {
    if (object == null) {
      throw new ClanException(String.format("Cannot find %s", Type));  // TODO: outsource to I18n?
    }
  }

  private void checkLeader(Player player, Clan clan) throws ClanException {
    if (clan.getLeader().getId() != player.getId()) {
      throw new ClanException("Player is not the leader of the clan");  // TODO: outsource to I18n?
    }
  }

  // Show error message as result
  @ExceptionHandler(ClanException.class)
  public void handleClanException(ClanException ex, HttpServletResponse response) throws IOException {
    response.sendError(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
  }
}

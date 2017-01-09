package com.faforever.api.clan;

import com.faforever.api.data.domain.Clan;
import com.faforever.api.data.domain.Player;
import com.faforever.api.player.PlayerRepository;
import com.faforever.api.user.FafUserDetails;
import com.faforever.api.utils.AuthenticationHelper;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;


@RestController
@RequestMapping(path = "/clans")
public class ClansController {

  private final ClanRepository clanRepository;
  private final PlayerRepository playerRepository;

  @Inject
  public ClansController(ClanRepository clanRepository, PlayerRepository playerRepository) {
    this.clanRepository = clanRepository;
    this.playerRepository = playerRepository;
  }

  @ApiOperation("Create a clan with correct leader, founder and clan membership")
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "Success"),
      @ApiResponse(code = 400, message = "Bad Request")})
  @RequestMapping(path = "/create", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  @Transactional
  public String createClan(@RequestParam(value = "name") String name,
                           @RequestParam(value = "tag") String tag,
                           @RequestParam(value = "description", required = false) String description,
                           Authentication authentication) throws IOException, ClanException {
    Clan clan = new Clan();
    clan.setName(name);
    clan.setTag(tag);
    clan.setDescription(description);

    Player player = AuthenticationHelper.getPlayer(authentication, playerRepository);
    if (player.getClan().size() > 0) {
      throw new ClanException("player has allready a clan");
    }

    clan.setFounder(player);
    clan.setLeader(player);
    clan.setMembers(Arrays.asList(player));

    clanRepository.save(clan);

    return null;
  }

  @ExceptionHandler(ClanException.class)
  public String handleClanException(ClanException ex, HttpServletResponse response) throws IOException {
    response.sendError(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
    return null;
  }
}

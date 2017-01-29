package com.faforever.api.clan;

import com.faforever.api.authentication.AuthenticationService;
import com.faforever.api.authentication.JwtService;
import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.Clan;
import com.faforever.api.data.domain.ClanMembership;
import com.faforever.api.data.domain.Player;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.error.ProgrammingError;
import com.faforever.api.player.PlayerRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import org.springframework.security.core.Authentication;
import org.springframework.security.jwt.Jwt;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Arrays;

@Service
public class ClanService {

  private final ClanRepository clanRepository;
  private final PlayerRepository playerRepository;
  private final FafApiProperties fafApiProperties;
  private final JwtService jwtService;
  private final ObjectMapper objectMapper;
  private final AuthenticationService authenticationService;
  private final ClanMembershipRepository clanMembershipRepository;

  @Inject
  public ClanService(ClanRepository clanRepository,
                     PlayerRepository playerRepository,
                     FafApiProperties fafApiProperties,
                     JwtService jwtService, AuthenticationService authenticationService, ClanMembershipRepository clanMembershipRepository) {
    this.clanRepository = clanRepository;
    this.playerRepository = playerRepository;
    this.fafApiProperties = fafApiProperties;
    this.jwtService = jwtService;
    this.authenticationService = authenticationService;
    this.clanMembershipRepository = clanMembershipRepository;
    this.objectMapper = new ObjectMapper();
  }

  @SneakyThrows
  public Clan create(String name, String tag, String description, Player creator) {
    if (creator.getClanMemberships().size() > 0) {
      throw new ApiException(new Error(ErrorCode.CLAN_CREATE_CREATOR_IS_IN_A_CLAN));
    }

    Clan clan = new Clan();
    clan.setName(name);
    clan.setTag(tag);
    clan.setDescription(description);

    clan.setFounder(creator);
    clan.setLeader(creator);

    ClanMembership membership = new ClanMembership();
    membership.setClan(clan);
    membership.setPlayer(creator);

    clan.setMemberships(Arrays.asList(membership));
    clanRepository.save(clan); // clan membership is saved over cascading, otherwise validation will fail
    return clan;
  }

  @SneakyThrows
  public String generatePlayerInvitationToken(Player requestor, int newMemberId, int clanId) {
    Clan clan = clanRepository.findOne(clanId);

    if (clan == null) {
      throw new ApiException(new Error(ErrorCode.CLAN_NOT_EXISTS));
    }
    if (requestor.getId() != clan.getLeader().getId()) {
      throw new ApiException(new Error(ErrorCode.CLAN_NOT_LEADER));
    }

    Player newMember = playerRepository.getOne(newMemberId);
    if (newMember == null) {
      throw new ProgrammingError("ClanMember does not exist: " + newMemberId);
    }

    // TODO: not sure if this is a good idea, e.g. Time Zones
    long expire = System.currentTimeMillis()
        + (fafApiProperties.getClan().getExpireDurationInMinutes() * 60 * 1000);

    String jwtToken = jwtService.sign(
        ImmutableMap.of("newMemberId", newMemberId,
            "expire", expire,
            "clan", ImmutableMap.of("id", clan.getId(),
                "tag", clan.getTag(),
                "name", clan.getName())));
    return jwtToken;
  }

  @SneakyThrows
  public void acceptPlayerInvitationToken(String stringToken, Authentication authentication) {
    Jwt token = jwtService.decodeAndVerify(stringToken);
    JsonNode data = objectMapper.readTree(token.getClaims());

    if (data.get("expire").asLong() < System.currentTimeMillis()) {
      throw new ApiException(new Error(ErrorCode.CLAN_ACCEPT_TOKEN_EXPIRE));
    }

    Player player = authenticationService.getPlayer(authentication);
    Clan clan = clanRepository.findOne(data.get("clan").get("id").asInt());

    if (clan == null) {
      throw new ApiException(new Error(ErrorCode.CLAN_NOT_EXISTS));
    }

    Player newMember = playerRepository.findOne(data.get("newMemberId").asInt());
    if (newMember == null) {
      throw new ProgrammingError("ClanMember does not exist: "
          + data.get("newMemberId").asInt());
    }

    if (player.getId() != newMember.getId()) {
      throw new ApiException(new Error(ErrorCode.CLAN_ACCEPT_WRONG_PLAYER));
    }
    if (newMember.getClan() != null) {
      throw new ApiException(new Error(ErrorCode.CLAN_ACCEPT_PLAYER_IN_A_CLAN));
    }

    ClanMembership membership = new ClanMembership();
    membership.setClan(clan);
    membership.setPlayer(newMember);
    clanMembershipRepository.save(membership);
  }
}

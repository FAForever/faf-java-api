package com.faforever.api.clan;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.Clan;
import com.faforever.api.data.domain.ClanMembership;
import com.faforever.api.data.domain.Player;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import com.faforever.api.error.ProgrammingError;
import com.faforever.api.player.PlayerRepository;
import com.faforever.api.player.PlayerService;
import com.faforever.api.security.JwtService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import org.springframework.security.core.Authentication;
import org.springframework.security.jwt.Jwt;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

@Service
public class ClanService {

  private final ClanRepository clanRepository;
  private final PlayerRepository playerRepository;
  private final FafApiProperties fafApiProperties;
  private final JwtService jwtService;
  private final ObjectMapper objectMapper;
  private final PlayerService playerService;
  private final ClanMembershipRepository clanMembershipRepository;

  @Inject
  public ClanService(ClanRepository clanRepository,
                     PlayerRepository playerRepository,
                     FafApiProperties fafApiProperties,
                     JwtService jwtService,
                     PlayerService playerService,
                     ClanMembershipRepository clanMembershipRepository,
                     ObjectMapper objectMapper) {
    this.clanRepository = clanRepository;
    this.playerRepository = playerRepository;
    this.fafApiProperties = fafApiProperties;
    this.jwtService = jwtService;
    this.playerService = playerService;
    this.clanMembershipRepository = clanMembershipRepository;
    this.objectMapper = objectMapper;
  }

  @SneakyThrows
  public Clan create(String name, String tag, String description, Player creator) {
    if (!creator.getClanMemberships().isEmpty()) {
      throw new ApiException(new Error(ErrorCode.CLAN_CREATE_CREATOR_IS_IN_A_CLAN));
    }
    if (clanRepository.findOneByName(name).isPresent()) {
      throw new ApiException(new Error(ErrorCode.CLAN_NAME_EXISTS, name));
    }
    if (clanRepository.findOneByTag(tag).isPresent()) {
      throw new ApiException(new Error(ErrorCode.CLAN_TAG_EXISTS, name));
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

    clan.setMemberships(Collections.singletonList(membership));

    // clan membership is saved over cascading, otherwise validation will fail
    clanRepository.save(clan);
    return clan;
  }

  @SneakyThrows
  public String generatePlayerInvitationToken(Player requester, int newMemberId, int clanId) {
    Clan clan = clanRepository.findOne(clanId);

    if (clan == null) {
      throw new ApiException(new Error(ErrorCode.CLAN_NOT_EXISTS, clanId));
    }
    if (requester.getId() != clan.getLeader().getId()) {
      throw new ApiException(new Error(ErrorCode.CLAN_NOT_LEADER, clanId));
    }

    Player newMember = playerRepository.findOne(newMemberId);
    if (newMember == null) {
      throw new ApiException(new Error(ErrorCode.CLAN_GENERATE_LINK_PLAYER_NOT_FOUND, newMemberId));
    }

    long expire = Instant.now()
        .plus(fafApiProperties.getClan().getInviteLinkExpireDurationMinutes(), ChronoUnit.MINUTES)
        .toEpochMilli();

    return jwtService.sign(
        ImmutableMap.of(JwtKeys.NEW_MEMBER_ID, newMemberId,
            JwtKeys.EXPIRE_IN, expire,
            JwtKeys.CLAN, ImmutableMap.of(
                JwtKeys.CLAN_ID, clan.getId(),
                JwtKeys.CLAN_TAG, clan.getTag(),
                JwtKeys.CLAN_NAME, clan.getName())
        ));
  }

  @SneakyThrows
  // TODO @dragonfire don't manually read JSON values, deserialize into a Java object?
  public void acceptPlayerInvitationToken(String stringToken, Authentication authentication) {
    Jwt token = jwtService.decodeAndVerify(stringToken);
    JsonNode data = objectMapper.readTree(token.getClaims());

    if (data.get(JwtKeys.EXPIRE_IN).asLong() < System.currentTimeMillis()) {
      throw new ApiException(new Error(ErrorCode.CLAN_ACCEPT_TOKEN_EXPIRE));
    }

    Player player = playerService.getPlayer(authentication);
    Clan clan = clanRepository.findOne(data.get(JwtKeys.CLAN).get(JwtKeys.CLAN_ID).asInt());

    if (clan == null) {
      throw new ApiException(new Error(ErrorCode.CLAN_NOT_EXISTS));
    }

    Player newMember = playerRepository.findOne(data.get(JwtKeys.NEW_MEMBER_ID).asInt());
    if (newMember == null) {
      throw new ProgrammingError("ClanMember does not exist: " + data.get(JwtKeys.NEW_MEMBER_ID).asInt());
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

  private class JwtKeys {
    public static final String NEW_MEMBER_ID = "newMemberId";
    public static final String EXPIRE_IN = "expire";
    public static final String CLAN = "clan";
    public static final String CLAN_ID = "id";
    public static final String CLAN_TAG = "tag";
    public static final String CLAN_NAME = "name";
  }
}

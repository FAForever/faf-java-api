package com.faforever.api.clan;

import com.faforever.api.clan.result.ClanResult;
import com.faforever.api.clan.result.InvitationResult;
import com.faforever.api.clan.result.PlayerResult;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.core.Authentication;
import org.springframework.security.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class ClanService {

  private final ClanRepository clanRepository;
  private final PlayerRepository playerRepository;
  private final FafApiProperties fafApiProperties;
  private final JwtService jwtService;
  private final ObjectMapper objectMapper;
  private final PlayerService playerService;
  private final ClanMembershipRepository clanMembershipRepository;

  @SneakyThrows
  Clan create(String name, String tag, String description, Player creator) {
    if (creator.getClanMembership() != null) {
      throw new ApiException(new Error(ErrorCode.CLAN_CREATE_FOUNDER_IS_IN_A_CLAN));
    }
    if (clanRepository.findOneByName(name).isPresent()) {
      throw new ApiException(new Error(ErrorCode.CLAN_NAME_EXISTS, name));
    }
    if (clanRepository.findOneByTag(tag).isPresent()) {
      throw new ApiException(new Error(ErrorCode.CLAN_TAG_EXISTS, tag));
    }

    Clan clan = new Clan();
    clan.setName(name);
    clan.setTag(tag);
    clan.setDescription(description);
    clan.setRequiresInvitation(true);

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
  String generatePlayerInvitationToken(Player requester, int newMemberId, int clanId) {
    Clan clan = clanRepository.findById(clanId)
      .orElseThrow(() -> new ApiException(new Error(ErrorCode.CLAN_NOT_EXISTS, clanId)));

    if (requester.getId() != clan.getLeader().getId()) {
      throw new ApiException(new Error(ErrorCode.CLAN_NOT_LEADER, clanId));
    }

    Player newMember = playerRepository.findById(newMemberId)
      .orElseThrow(() -> new ApiException(new Error(ErrorCode.CLAN_GENERATE_LINK_PLAYER_NOT_FOUND, newMemberId)));

    long expire = Instant.now()
      .plus(fafApiProperties.getClan().getInviteLinkExpireDurationMinutes(), ChronoUnit.MINUTES)
      .toEpochMilli();

    InvitationResult result = new InvitationResult(expire,
      ClanResult.of(clan),
      PlayerResult.of(newMember));
    return jwtService.sign(result);
  }

  @SneakyThrows
  void acceptPlayerInvitationToken(String stringToken, Authentication authentication) {
    Jwt token = jwtService.decodeAndVerify(stringToken);
    InvitationResult invitation = objectMapper.readValue(token.getClaims(), InvitationResult.class);

    if (invitation.isExpired()) {
      throw new ApiException(new Error(ErrorCode.CLAN_ACCEPT_TOKEN_EXPIRE));
    }

    final Integer clanId = invitation.getClan().getId();
    Player player = playerService.getPlayer(authentication);
    Clan clan = clanRepository.findById(clanId)
      .orElseThrow(() -> new ApiException(new Error(ErrorCode.CLAN_NOT_EXISTS, clanId)));

    Player newMember = playerRepository.findById(invitation.getNewMember().getId())
      .orElseThrow(() -> new ProgrammingError("ClanMember does not exist: " + invitation.getNewMember().getId()));


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

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
import com.faforever.api.player.PlayerService;
import com.faforever.api.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClanService {

  private final ClanRepository clanRepository;
  private final FafApiProperties fafApiProperties;
  private final JwtService jwtService;
  private final ObjectMapper objectMapper;
  private final PlayerService playerService;
  private final ClanMembershipRepository clanMembershipRepository;

  @Transactional
  public void preCreate(Clan clan) {
    Assert.isNull(clan.getId(), "Clan payload with id can not be used for creation.");

    Player player = playerService.getCurrentPlayer();

    if (player.getClanMembership() != null) {
      throw ApiException.of(ErrorCode.CLAN_CREATE_FOUNDER_IS_IN_A_CLAN);
    }

    if (!player.equals(clan.getFounder())) {
      throw ApiException.of(ErrorCode.CLAN_INVALID_FOUNDER);
    }

    clanRepository.findOneByName(clan.getName()).ifPresent(c -> {
      throw ApiException.of(ErrorCode.CLAN_NAME_EXISTS, clan.getName());
    });

    clanRepository.findOneByTag(clan.getTag()).ifPresent(c -> {
      throw ApiException.of(ErrorCode.CLAN_TAG_EXISTS, clan.getTag());
    });

    clan.setLeader(player);
    clan.setMemberships(List.of(new ClanMembership()
      .setClan(clan)
      .setPlayer(player)));
  }

  @SneakyThrows
  @Transactional
  @Deprecated
    // use POST via Elide instead
  Clan create(String name, String tag, String description) {
    Clan clan = new Clan();
    clan.setName(name);
    clan.setTag(tag);
    clan.setDescription(description);
    clan.setRequiresInvitation(true);
    clan.setFounder(playerService.getCurrentPlayer());
    clan.setLeader(playerService.getCurrentPlayer());

    // validation is done at preCreate() called by ClanListener
    clanRepository.save(clan);
    return clan;
  }

  @SneakyThrows
  @Transactional
  String generatePlayerInvitationToken(int newMemberId, int clanId) {
    Player requester = playerService.getCurrentPlayer();

    Clan clan = clanRepository.findById(clanId)
      .orElseThrow(() -> new ApiException(new Error(ErrorCode.CLAN_NOT_EXISTS, clanId)));

    if (requester.getId() != clan.getLeader().getId()) {
      throw ApiException.of(ErrorCode.CLAN_NOT_LEADER, clanId);
    }

    Player newMember = playerService.getById(newMemberId);

    long expire = Instant.now()
      .plus(fafApiProperties.getClan().getInviteLinkExpireDurationMinutes(), ChronoUnit.MINUTES)
      .toEpochMilli();

    InvitationResult result = new InvitationResult(expire,
      ClanResult.of(clan),
      PlayerResult.of(newMember));
    return jwtService.sign(result);
  }

  @SneakyThrows
  @Transactional
  void acceptPlayerInvitationToken(String stringToken) {
    Jwt token = jwtService.decodeAndVerify(stringToken);
    InvitationResult invitation = objectMapper.readValue(token.getClaims(), InvitationResult.class);

    if (invitation.isExpired()) {
      throw ApiException.of(ErrorCode.CLAN_ACCEPT_TOKEN_EXPIRE);
    }

    final Integer clanId = invitation.getClan().getId();
    Player player = playerService.getCurrentPlayer();
    Clan clan = clanRepository.findById(clanId)
      .orElseThrow(() -> new ApiException(new Error(ErrorCode.CLAN_NOT_EXISTS, clanId)));

    Player newMember = playerService.getById(invitation.getNewMember().getId());

    if (player.getId() != newMember.getId()) {
      throw ApiException.of(ErrorCode.CLAN_ACCEPT_WRONG_PLAYER);
    }
    if (newMember.getClan() != null) {
      throw ApiException.of(ErrorCode.CLAN_ACCEPT_PLAYER_IN_A_CLAN);
    }

    ClanMembership membership = new ClanMembership();
    membership.setClan(clan);
    membership.setPlayer(newMember);
    clanMembershipRepository.save(membership);
  }
}

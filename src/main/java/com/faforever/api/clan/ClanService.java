package com.faforever.api.clan;

import com.faforever.api.data.domain.Clan;
import com.faforever.api.data.domain.ClanMembership;
import com.faforever.api.data.domain.Player;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Arrays;

@Service
public class ClanService {

  private final ClanRepository clanRepository;

  @Inject
  public ClanService(ClanRepository clanRepository) {
    this.clanRepository = clanRepository;
  }

  /**
   * Created a new Clan, with the creator as leader, founder and member.
   * @param name unique name of the clan
   * @param tag unique clan tag with a certain length
   * @param description canbe null
   * @return Ithe new clam
   * @throws com.faforever.api.error.ApiException if something goes wrong
   * @see Clan
   */
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
}

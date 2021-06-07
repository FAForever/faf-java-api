package com.faforever.api.clan.result;

import com.faforever.api.data.domain.Clan;

public record ClanResult(Integer id, String tag, String name) {
  public static ClanResult of(Clan clan) {
    return new ClanResult(clan.getId(), clan.getTag(), clan.getName());
  }
}

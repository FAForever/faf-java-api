package com.faforever.api.clan.result;

import com.faforever.api.data.domain.Clan;
import lombok.Data;

@Data
public class ClanResult {
  private final Integer id;
  private final String tag;
  private final String name;

  public static ClanResult of(Clan clan) {
    return new ClanResult(clan.getId(), clan.getTag(), clan.getName());
  }
}

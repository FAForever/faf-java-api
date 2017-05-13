package com.faforever.integration.factories;

import com.faforever.api.data.domain.Clan;
import com.faforever.api.data.domain.Player;
import lombok.Builder;

public class ClanFactory {
  public static final String DEFAULT_CLAN_NAME = "JUnitClan_ClanFactory";
  public static final String DEFAULT_CLAN_TAG = "123";

  @Builder
  private static Clan create(int id, String name, String tag,
                             Player founder, Player leader, String description) {
    return (Clan) new Clan()
      .setName(name != null ? name : DEFAULT_CLAN_NAME)
      .setTag(tag != null ? tag : DEFAULT_CLAN_TAG)
      .setFounder(founder)
      .setLeader(leader)
      .setDescription(description)
      .setId(id);
  }
}

package com.faforever.integration.factories;

import com.faforever.api.data.domain.Clan;
import com.faforever.api.data.domain.Player;
import lombok.Builder;

public class ClanFactory {

  public static Clan create() {
    return ClanFactory.builder().id(1).build();
  }

  public static Clan create(Player leader) {
    return ClanFactory.builder()
        .id(1)
        .name("TestClan")
        .tag("1234")
        .leader(leader).build();
  }

  @Builder
  public static Clan create(int id, String name, String tag,
                            Player founder, Player leader, String description) {
    return (Clan) new Clan()
        .setName(name)
        .setTag(tag)
        .setFounder(founder)
        .setLeader(leader)
        .setDescription(description)
        .setId(id);
  }
}

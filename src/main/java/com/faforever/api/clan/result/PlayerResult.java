package com.faforever.api.clan.result;

import com.faforever.api.data.domain.Player;

public record PlayerResult(Integer id, String login) {
  public static PlayerResult of(Player newMember) {
    return new PlayerResult(newMember.getId(), newMember.getLogin());
  }
}

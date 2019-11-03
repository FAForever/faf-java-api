package com.faforever.api.clan.result;

import com.faforever.api.data.domain.Player;
import lombok.Data;

@Data
public class PlayerResult {
  private final Integer id;
  private final String login;

  public static PlayerResult of(Player newMember) {
    return new PlayerResult(newMember.getId(), newMember.getLogin());
  }
}

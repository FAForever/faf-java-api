package com.faforever.api.clan.result;

import lombok.Data;

@Data
public class InvitationResult {
  private final long expire;
  private final ClanResult clan;
  private final PlayerResult newMember;

  public boolean isExpired() {
    return expire < System.currentTimeMillis();
  }
}

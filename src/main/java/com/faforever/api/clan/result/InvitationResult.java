package com.faforever.api.clan.result;

public record InvitationResult(
  long expire,
  ClanResult clan,
  PlayerResult newMember
) {
  public boolean isExpired() {
    return expire < System.currentTimeMillis();
  }
}

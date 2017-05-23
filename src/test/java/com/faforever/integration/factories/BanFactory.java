package com.faforever.integration.factories;

import com.faforever.api.data.domain.BanInfo;
import com.faforever.api.data.domain.BanLevel;
import com.faforever.api.data.domain.Player;
import com.faforever.integration.TestDatabase;
import lombok.Builder;
import lombok.SneakyThrows;
import org.springframework.util.Assert;

import java.time.OffsetDateTime;

public class BanFactory {
  public static final String PLAYER_NAME_BANNED = "JUnitBannedPlayer";
  public static final String PLAYER_NAME_AUTHOR = "JUnitBanAuthor";
  public static final String DEFAULT_REASON = "This is a cool ban reason";


  @Builder
  @SneakyThrows
  public static BanInfo createBan(Player player, Player author, String reason,
                                  BanLevel level, OffsetDateTime expiresAt, TestDatabase database) {
    Assert.notNull(database, "'database' must not be null");
    BanInfo ban = new BanInfo()
      .setPlayer(player != null ? player : PlayerFactory.createPlayer(PLAYER_NAME_BANNED, database))
      .setAuthor(author != null ? author : PlayerFactory.createPlayer(PLAYER_NAME_AUTHOR, database))
      .setExpiresAt(expiresAt)
      .setLevel(level)
      .setReason(reason != null ? reason : DEFAULT_REASON);
    return database.getBanRepository().save(ban);
  }

}

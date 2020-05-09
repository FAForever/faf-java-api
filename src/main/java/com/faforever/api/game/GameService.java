package com.faforever.api.game;

import com.faforever.api.config.FafApiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
@RequiredArgsConstructor
public class GameService {
  private final FafApiProperties properties;

  public String getReplayDownloadUrl(int replayId) {
    Assert.state(replayId > 0, "Replay ID must be positive");

    String leadingZeroReplayId = String.format("%010d", replayId);

    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < 4; i++) {
      String subfolderId = leadingZeroReplayId.substring(i * 2, i * 2 + 2);
      sb.append(Integer.parseInt(subfolderId));
      sb.append("/");
    }

    sb.append(replayId);
    sb.append(".fafreplay");

    return String.format(properties.getReplay().getDownloadUrlFormat(), sb.toString());
  }

}

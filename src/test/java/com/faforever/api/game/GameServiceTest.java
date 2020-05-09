package com.faforever.api.game;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.config.FafApiProperties.Replay;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {
  @Mock
  FafApiProperties properties;

  @Mock
  Replay replay;

  private GameService instance;

  @BeforeEach
  void setUp() {
    instance = new GameService(properties);
  }

  @Test
  void getInvalidReplayDownloadUrl() {
    assertThrows(IllegalStateException.class, () -> instance.getReplayDownloadUrl(-1));
    assertThrows(IllegalStateException.class, () -> instance.getReplayDownloadUrl(0));
  }

  @ParameterizedTest
  @CsvSource({
    "1,http://localhost/replays/0/0/0/0/1.fafreplay",
    "99,http://localhost/replays/0/0/0/0/99.fafreplay",
    "199,http://localhost/replays/0/0/0/1/199.fafreplay",
    "9999,http://localhost/replays/0/0/0/99/9999.fafreplay",
    "5050501,http://localhost/replays/0/5/5/5/5050501.fafreplay",
    "11689995,http://localhost/replays/0/11/68/99/11689995.fafreplay",
  })
  void getReplayDownloadUrl(int replayId, String expectedUrl) {
    when(properties.getReplay()).thenReturn(replay);
    when(replay.getDownloadUrlFormat()).thenReturn("http://localhost/replays/%s");

    assertThat(instance.getReplayDownloadUrl(replayId), is(expectedUrl));
  }
}

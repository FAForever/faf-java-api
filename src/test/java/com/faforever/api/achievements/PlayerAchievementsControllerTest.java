package com.faforever.api.achievements;

import com.faforever.api.achievements.AchievementUpdateRequest.Operation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
public class PlayerAchievementsControllerTest {

  private AchievementsController instance;

  @Mock
  private AchievementService achievementService;

  @BeforeEach
  public void setUp() throws Exception {
    instance = new AchievementsController(achievementService);
  }

  @Test
  public void update() throws Exception {
    var updateRequests = List.of(
      new AchievementUpdateRequest(1, "111", Operation.INCREMENT, 7),
      new AchievementUpdateRequest(1, "222", Operation.INCREMENT, 19),
      new AchievementUpdateRequest(1, "333", Operation.SET_STEPS_AT_LEAST, 9),
      new AchievementUpdateRequest(1, "444", Operation.SET_STEPS_AT_LEAST, 13),
      new AchievementUpdateRequest(1, "555", Operation.UNLOCK, 11),
      new AchievementUpdateRequest(1, "666", Operation.UNLOCK, 17)
    );

    updateRequests.forEach(request -> instance.update(request));

    verify(achievementService).increment(1, "111", 7);
    verify(achievementService).increment(1, "222", 19);
    verify(achievementService).setStepsAtLeast(1, "333", 9);
    verify(achievementService).setStepsAtLeast(1, "444", 13);
    verify(achievementService).unlock(1, "555");
    verify(achievementService).unlock(1, "666");
  }

  @Test
  public void updateReveledUnsupported() throws Exception {
    AchievementUpdateRequest updateRequest = new AchievementUpdateRequest(1, "111", Operation.REVEAL, 1);

    assertThrows(UnsupportedOperationException.class, () -> instance.update(updateRequest));

  }
}

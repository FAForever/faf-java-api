package com.faforever.api.achievements;

import com.faforever.api.achievements.AchievementUpdateRequest.Operation;
import com.faforever.api.data.domain.AchievementState;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static com.faforever.api.TestUser.testUser;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class AchievementsControllerTest {

  private AchievementsController instance;

  @Mock
  private AchievementsService achievementService;

  @Before
  public void setUp() throws Exception {
    instance = new AchievementsController(achievementService);
  }

  @Test
  public void update() throws Exception {
    AchievementUpdateRequest[] updateRequests = new AchievementUpdateRequest[]{
        new AchievementUpdateRequest("111", Operation.INCREMENT, 7),
        new AchievementUpdateRequest("222", Operation.INCREMENT, 19),
        new AchievementUpdateRequest("333", Operation.SET_STEPS_AT_LEAST, 9),
        new AchievementUpdateRequest("444", Operation.SET_STEPS_AT_LEAST, 13),
        new AchievementUpdateRequest("555", Operation.UNLOCK, 11),
        new AchievementUpdateRequest("666", Operation.UNLOCK, 17),
    };
    when(achievementService.increment(any(), anyInt(), anyInt())).thenAnswer(invocation ->
        new AchievementUpdateResponse(true, AchievementState.UNLOCKED, invocation.getArgumentAt(1, int.class)));

    List<AchievementUpdateResponse> result = instance.update(updateRequests, testUser());

    verify(achievementService).increment("111", 7, 1);
    verify(achievementService).increment("222", 19, 1);
    verify(achievementService).setStepsAtLeast("333", 9, 1);
    verify(achievementService).setStepsAtLeast("444", 13, 1);
    verify(achievementService).unlock("555", 1);
    verify(achievementService).unlock("666", 1);

    assertThat(result, hasSize(6));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void updateReveledUnsupported() throws Exception {
    AchievementUpdateRequest[] updateRequests = {new AchievementUpdateRequest("111", Operation.REVEAL, 1)};

    instance.update(updateRequests, testUser());
  }
}

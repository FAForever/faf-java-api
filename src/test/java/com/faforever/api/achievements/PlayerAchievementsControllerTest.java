package com.faforever.api.achievements;

import com.faforever.api.achievements.AchievementUpdateRequest.Operation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.faforever.api.TestUser.testUser;


@RunWith(MockitoJUnitRunner.class)
public class PlayerAchievementsControllerTest {

  private PlayerAchievementsController instance;

  @Mock
  private AchievementsService achievementService;

  @Before
  public void setUp() throws Exception {
    instance = new PlayerAchievementsController(achievementService);
  }

  // FIXME add back as soon as OAuth scope protected
//  @Test
//  public void update() throws Exception {
//    AchievementUpdateRequest[] updateRequests = new AchievementUpdateRequest[]{
//        new AchievementUpdateRequest("111", Operation.INCREMENT, 7),
//        new AchievementUpdateRequest("222", Operation.INCREMENT, 19),
//        new AchievementUpdateRequest("333", Operation.SET_STEPS_AT_LEAST, 9),
//        new AchievementUpdateRequest("444", Operation.SET_STEPS_AT_LEAST, 13),
//        new AchievementUpdateRequest("555", Operation.UNLOCK, 11),
//        new AchievementUpdateRequest("666", Operation.UNLOCK, 17),
//    };
//    when(achievementService.increment(any(), anyInt(), anyInt())).thenAnswer(invocation ->
//        new AchievementUpdateResponse(true, AchievementState.UNLOCKED, invocation.getArgumentAt(1, int.class)));
//
//    List<AchievementUpdateResponse> result = instance.update(updateRequests, testUser());
//
//    verify(achievementService).increment("111", 7, 1);
//    verify(achievementService).increment("222", 19, 1);
//    verify(achievementService).setStepsAtLeast("333", 9, 1);
//    verify(achievementService).setStepsAtLeast("444", 13, 1);
//    verify(achievementService).unlock("555", 1);
//    verify(achievementService).unlock("666", 1);
//
//    assertThat(result, hasSize(6));
//  }

  @Test(expected = UnsupportedOperationException.class)
  public void updateReveledUnsupported() throws Exception {
    AchievementUpdateRequest[] updateRequests = {new AchievementUpdateRequest("111", Operation.REVEAL, 1)};

    instance.update(updateRequests, testUser());
  }
}

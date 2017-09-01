package com.faforever.api.achievements;

import com.faforever.api.achievements.AchievementUpdateRequest.Operation;
import com.faforever.api.data.domain.AchievementState;
import com.yahoo.elide.jsonapi.models.JsonApiDocument;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class PlayerEventsControllerTest {

  private AchievementsController instance;

  @Mock
  private AchievementService achievementService;

  @Before
  public void setUp() throws Exception {
    instance = new AchievementsController(achievementService);
  }

  @Test
  public void update() throws Exception {
    AchievementUpdateRequest[] updateRequests = new AchievementUpdateRequest[]{
      new AchievementUpdateRequest(1, "111", Operation.INCREMENT, 7),
      new AchievementUpdateRequest(1, "222", Operation.INCREMENT, 19),
      new AchievementUpdateRequest(1, "333", Operation.SET_STEPS_AT_LEAST, 9),
      new AchievementUpdateRequest(1, "444", Operation.SET_STEPS_AT_LEAST, 13),
      new AchievementUpdateRequest(1, "555", Operation.UNLOCK, 11),
      new AchievementUpdateRequest(1, "666", Operation.UNLOCK, 17),
    };
    when(achievementService.increment(anyInt(), any(), anyInt())).thenAnswer(invocation ->
      new UpdatedAchievementResponse(invocation.getArgument(1), true, AchievementState.UNLOCKED, invocation.getArgument(2)));
    when(achievementService.setStepsAtLeast(anyInt(), any(), anyInt())).thenAnswer(invocation ->
      new UpdatedAchievementResponse(invocation.getArgument(1), true, AchievementState.UNLOCKED, invocation.getArgument(2)));
    when(achievementService.unlock(anyInt(), any())).thenAnswer(invocation ->
      new UpdatedAchievementResponse(invocation.getArgument(1), true, AchievementState.UNLOCKED));

    JsonApiDocument result = instance.update(updateRequests);

    verify(achievementService).increment(1, "111", 7);
    verify(achievementService).increment(1, "222", 19);
    verify(achievementService).setStepsAtLeast(1, "333", 9);
    verify(achievementService).setStepsAtLeast(1, "444", 13);
    verify(achievementService).unlock(1, "555");
    verify(achievementService).unlock(1, "666");

    assertThat(result.getData().get(), hasSize(6));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void updateReveledUnsupported() throws Exception {
    AchievementUpdateRequest[] updateRequests = {new AchievementUpdateRequest(1, "111", Operation.REVEAL, 1)};

    instance.update(updateRequests);
  }
}

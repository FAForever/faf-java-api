package com.faforever.api.achievements;

import com.faforever.api.data.domain.Achievement;
import com.faforever.api.data.domain.AchievementState;
import com.faforever.api.data.domain.AchievementType;
import com.faforever.api.data.domain.PlayerAchievement;
import com.faforever.api.error.ErrorCode;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static com.faforever.api.data.domain.AchievementState.REVEALED;
import static com.faforever.api.data.domain.AchievementState.UNLOCKED;
import static com.faforever.api.error.ApiExceptionWithCode.apiExceptionWithCode;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EventsServiceTest {

  private static final int PLAYER_ID = 1;
  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  private AchievementService instance;
  @Mock
  private AchievementRepository achievementRepository;
  @Mock
  private PlayerAchievementRepository playerAchievementRepository;

  private static PlayerAchievement createPlayerAchievement(Integer currentSteps, AchievementState state) {
    PlayerAchievement playerAchievement = new PlayerAchievement();
    playerAchievement.setState(state);
    playerAchievement.setCurrentSteps(currentSteps);
    return playerAchievement;
  }

  @Before
  public void setUp() throws Exception {
    instance = new AchievementService(achievementRepository, playerAchievementRepository);
  }

  /**
   * Tests whether an achievement is properly created and incremented if it's being incremented for the first time.
   */
  @Test
  public void incrementFirstTime() throws Exception {
    mockAchievement("111", AchievementType.INCREMENTAL, 10);
    when(playerAchievementRepository.findOneByAchievementIdAndPlayerId("111", PLAYER_ID))
      .thenReturn(Optional.empty());

    instance.increment(PLAYER_ID, "111", 3);

    PlayerAchievement playerAchievement = captureSaveEvent();

    assertThat(playerAchievement.getCurrentSteps(), is(3));
    assertThat(playerAchievement.getState(), is(REVEALED));
  }

  private void mockAchievement(String achievementId, AchievementType type, Integer totalSteps) {
    Achievement achievement = new Achievement();
    achievement.setId(achievementId);
    achievement.setType(type);
    achievement.setTotalSteps(totalSteps);

    when(achievementRepository.getOne(achievementId)).thenReturn(achievement);
  }

  private PlayerAchievement captureSaveEvent() {
    ArgumentCaptor<PlayerAchievement> captor = ArgumentCaptor.forClass(PlayerAchievement.class);
    verify(playerAchievementRepository).save(captor.capture());
    return captor.getValue();
  }

  /**
   * Tests whether an achievement is properly incremented if it already existed.
   */
  @Test
  public void incrementExisting() throws Exception {
    mockAchievement("111", AchievementType.INCREMENTAL, 10);
    when(playerAchievementRepository.findOneByAchievementIdAndPlayerId("111", PLAYER_ID))
      .thenReturn(Optional.of(createPlayerAchievement(4, REVEALED)));

    instance.increment(PLAYER_ID, "111", 3);

    PlayerAchievement playerAchievement = captureSaveEvent();

    assertThat(playerAchievement.getCurrentSteps(), is(7));
    assertThat(playerAchievement.getState(), is(REVEALED));
  }

  /**
   * Tests whether incrementing a non-incremental achievement is denied.
   */
  @Test
  public void incrementNonIncremental() throws Exception {
    mockAchievement("111", AchievementType.STANDARD, null);

    expectedException.expect(apiExceptionWithCode(ErrorCode.ACHIEVEMENT_NOT_INCREMENTAL));

    instance.increment(PLAYER_ID, "111", 3);

    verify(playerAchievementRepository, never()).findOneByAchievementIdAndPlayerId(any(), anyInt());
  }

  /**
   * Tests whether an achievement's steps are properly set to the specified value if it's being set for the first time.
   */
  @Test
  public void setStepsAtLeastFirstTime() throws Exception {
    mockAchievement("111", AchievementType.INCREMENTAL, 10);
    when(playerAchievementRepository.findOneByAchievementIdAndPlayerId("111", PLAYER_ID))
      .thenReturn(Optional.empty());

    instance.setStepsAtLeast(PLAYER_ID, "111", 4);

    PlayerAchievement playerAchievement = captureSaveEvent();

    assertThat(playerAchievement.getCurrentSteps(), is(4));
    assertThat(playerAchievement.getState(), is(REVEALED));
  }

  /**
   * Tests whether an achievement's steps are not updated if the existing steps are higher than the new steps.
   */
  @Test
  public void setStepsAtLeastExistingLessSteps() throws Exception {
    mockAchievement("111", AchievementType.INCREMENTAL, 10);
    when(playerAchievementRepository.findOneByAchievementIdAndPlayerId("111", PLAYER_ID))
      .thenReturn(Optional.of(createPlayerAchievement(5, REVEALED)));

    instance.setStepsAtLeast(PLAYER_ID, "111", 4);

    PlayerAchievement playerAchievement = captureSaveEvent();

    assertThat(playerAchievement.getCurrentSteps(), is(5));
    assertThat(playerAchievement.getState(), is(REVEALED));
  }

  /**
   * Tests whether an achievement's steps are updated if the existing steps are lower than the new steps.
   */
  @Test
  public void setStepsAtLeastExistingMoreSteps() throws Exception {
    mockAchievement("111", AchievementType.INCREMENTAL, 10);
    when(playerAchievementRepository.findOneByAchievementIdAndPlayerId("111", PLAYER_ID))
      .thenReturn(Optional.of(createPlayerAchievement(5, REVEALED)));

    instance.setStepsAtLeast(PLAYER_ID, "111", 6);

    PlayerAchievement playerAchievement = captureSaveEvent();

    assertThat(playerAchievement.getCurrentSteps(), is(6));
    assertThat(playerAchievement.getState(), is(REVEALED));
  }

  /**
   * Tests whether a PlayerAchievement is properly created and unlocked if it's being unlocked the first time.
   */
  @Test
  public void unlockFirstTime() throws Exception {
    mockAchievement("111", AchievementType.STANDARD, null);
    when(playerAchievementRepository.findOneByAchievementIdAndPlayerId("111", PLAYER_ID))
      .thenReturn(Optional.empty());

    instance.unlock(PLAYER_ID, "111");

    PlayerAchievement playerAchievement = captureSaveEvent();

    assertThat(playerAchievement.getCurrentSteps(), is(CoreMatchers.nullValue()));
    assertThat(playerAchievement.getState(), is(UNLOCKED));
  }

  /**
   * Tests whether a PlayerAchievement isn't touched if it was already unlocked
   */
  @Test
  public void unlockSecondTime() throws Exception {
    mockAchievement("111", AchievementType.STANDARD, null);
    when(playerAchievementRepository.findOneByAchievementIdAndPlayerId("111", PLAYER_ID))
      .thenReturn(Optional.of(createPlayerAchievement(null, UNLOCKED)));

    instance.unlock(PLAYER_ID, "111");

    verify(playerAchievementRepository, never()).save(any(PlayerAchievement.class));
  }

  /**
   * Tests whether unlocking an incremental achievement is denied.
   */
  @Test
  public void unlockIncremental() throws Exception {
    mockAchievement("111", AchievementType.INCREMENTAL, 1);

    expectedException.expect(apiExceptionWithCode(ErrorCode.ACHIEVEMENT_NOT_STANDARD));

    instance.unlock(PLAYER_ID, "111");

    verify(playerAchievementRepository, never()).findOneByAchievementIdAndPlayerId(any(), anyInt());
  }
}

package com.faforever.api.data.hook;

import com.faforever.api.avatar.AvatarAssignmentRepository;
import com.faforever.api.data.domain.Avatar;
import com.faforever.api.data.domain.AvatarAssignment;
import com.faforever.api.data.domain.Player;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.ErrorCode;
import com.yahoo.elide.annotation.LifeCycleHookBinding.Operation;
import com.yahoo.elide.annotation.LifeCycleHookBinding.TransactionPhase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.faforever.api.error.ApiExceptionMatcher.hasErrorCode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerAvatarValidationHookTest {

  private static final int PLAYER_ID = 7;
  private static final int AVATAR_ID = 42;

  @Mock
  private AvatarAssignmentRepository avatarAssignmentRepository;

  private PlayerAvatarValidationHook instance;

  @BeforeEach
  void setUp() {
    instance = new PlayerAvatarValidationHook(avatarAssignmentRepository);
  }

  private static Player playerWithAvatar(Integer avatarId) {
    Avatar avatar = new Avatar();
    avatar.setId(avatarId);
    Player player = new Player();
    player.setId(PLAYER_ID);
    player.setCurrentAvatar(avatar);
    return player;
  }

  @Test
  void allowsAssignedAvatar() {
    when(avatarAssignmentRepository.findOneByAvatarIdAndPlayerId(AVATAR_ID, PLAYER_ID))
      .thenReturn(Optional.of(new AvatarAssignment()));

    instance.execute(Operation.UPDATE, TransactionPhase.PRECOMMIT, playerWithAvatar(AVATAR_ID), null, Optional.empty());
  }

  @Test
  void rejectsUnassignedAvatar() {
    when(avatarAssignmentRepository.findOneByAvatarIdAndPlayerId(AVATAR_ID, PLAYER_ID))
      .thenReturn(Optional.empty());

    ApiException result = assertThrows(ApiException.class, () ->
      instance.execute(Operation.UPDATE, TransactionPhase.PRECOMMIT, playerWithAvatar(AVATAR_ID), null, Optional.empty()));
    assertThat(result, hasErrorCode(ErrorCode.AVATAR_NOT_ASSIGNED));
  }

  @Test
  void allowsClearingAvatar() {
    Player player = new Player();
    player.setId(PLAYER_ID);

    instance.execute(Operation.UPDATE, TransactionPhase.PRECOMMIT, player, null, Optional.empty());

    verifyNoInteractions(avatarAssignmentRepository);
  }
}

package com.faforever.api.data.hook;

import com.faforever.api.avatar.AvatarAssignmentRepository;
import com.faforever.api.data.domain.Avatar;
import com.faforever.api.data.domain.Player;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.Error;
import com.faforever.api.error.ErrorCode;
import com.yahoo.elide.annotation.LifeCycleHookBinding.Operation;
import com.yahoo.elide.annotation.LifeCycleHookBinding.TransactionPhase;
import com.yahoo.elide.core.lifecycle.LifeCycleHook;
import com.yahoo.elide.core.security.ChangeSpec;
import com.yahoo.elide.core.security.RequestScope;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Rejects assigning a {@code currentAvatar} that is not granted to the player. The
 * {@code IsEntityOwner} update permission only guarantees the caller edits their own Player row;
 * it does not verify the avatar belongs to them. Clearing the avatar (null) is always allowed.
 */
@Component
@RequiredArgsConstructor
public class PlayerAvatarValidationHook implements LifeCycleHook<Player> {

  private final AvatarAssignmentRepository avatarAssignmentRepository;

  @Override
  public void execute(Operation operation, TransactionPhase phase, Player player, RequestScope requestScope, Optional<ChangeSpec> changes) {
    final Avatar avatar = player.getCurrentAvatar();
    if (avatar == null) {
      return;
    }

    avatarAssignmentRepository.findOneByAvatarIdAndPlayerId(avatar.getId(), player.getId())
      .orElseThrow(() -> new ApiException(new Error(ErrorCode.AVATAR_NOT_ASSIGNED, avatar.getId(), player.getId())));
  }
}

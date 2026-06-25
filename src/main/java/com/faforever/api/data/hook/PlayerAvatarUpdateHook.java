package com.faforever.api.data.hook;

import com.faforever.api.config.RabbitConfiguration;
import com.faforever.api.data.domain.Avatar;
import com.faforever.api.data.domain.Player;
import com.yahoo.elide.annotation.LifeCycleHookBinding.Operation;
import com.yahoo.elide.annotation.LifeCycleHookBinding.TransactionPhase;
import com.yahoo.elide.core.lifecycle.LifeCycleHook;
import com.yahoo.elide.core.security.ChangeSpec;
import com.yahoo.elide.core.security.RequestScope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlayerAvatarUpdateHook implements LifeCycleHook<Player> {

  public static final String ROUTING_KEY_PLAYER_AVATAR_UPDATE = "success.player_avatar.update";

  private final RabbitTemplate rabbitTemplate;

  @Override
  public void execute(Operation operation, TransactionPhase phase, Player player, RequestScope requestScope, Optional<ChangeSpec> changes) {
    final Avatar avatar = player.getCurrentAvatar();
    final Integer avatarId = avatar == null ? null : avatar.getId();

    Map<String, Object> payload = new HashMap<>();
    payload.put("player_id", player.getId());
    payload.put("avatar_id", avatarId);

    log.debug("Publishing player_avatar update: {}", payload);
    rabbitTemplate.convertAndSend(
      RabbitConfiguration.EXCHANGE_FAF_LOBBY,
      ROUTING_KEY_PLAYER_AVATAR_UPDATE,
      payload
    );
  }
}

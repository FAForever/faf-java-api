package com.faforever.api.config;

import com.faforever.api.AbstractIntegrationTest;
import com.faforever.api.data.domain.Avatar;
import com.faforever.api.data.domain.Player;
import com.faforever.api.data.hook.PlayerAvatarUpdateHook;
import com.yahoo.elide.annotation.LifeCycleHookBinding.Operation;
import com.yahoo.elide.annotation.LifeCycleHookBinding.TransactionPhase;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static com.faforever.api.config.RabbitConfiguration.EXCHANGE_FAF_LOBBY;
import static com.faforever.api.data.hook.PlayerAvatarUpdateHook.ROUTING_KEY_PLAYER_AVATAR_UPDATE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that messages published through the auto-configured {@link RabbitTemplate} are serialized as JSON
 * (content type {@code application/json}) rather than the default {@code application/x-java-serialized-object},
 * i.e. that the application-wide {@link RabbitConfiguration#jacksonJsonMessageConverter()} bean is actually
 * applied to the publishing side.
 */
public class RabbitMessageEncodingTest extends AbstractIntegrationTest {

  private static final String TEST_QUEUE = "inttest.player_avatar.update";

  @Autowired
  private RabbitTemplate rabbitTemplate;
  @Autowired
  private AmqpAdmin amqpAdmin;
  @Autowired
  private PlayerAvatarUpdateHook playerAvatarUpdateHook;

  private void bindTestQueue() {
    Queue queue = new Queue(TEST_QUEUE, false, false, true);
    amqpAdmin.declareQueue(queue);
    amqpAdmin.declareBinding(
      BindingBuilder.bind(queue).to(new TopicExchange(EXCHANGE_FAF_LOBBY)).with(ROUTING_KEY_PLAYER_AVATAR_UPDATE));
  }

  @AfterEach
  public void deleteTestQueue() {
    amqpAdmin.deleteQueue(TEST_QUEUE);
  }

  @Test
  public void avatarUpdateIsPublishedAsJson() throws JSONException {
    bindTestQueue();

    Player player = new Player();
    player.setId(42);
    Avatar avatar = new Avatar();
    avatar.setId(5);
    player.setCurrentAvatar(avatar);

    playerAvatarUpdateHook.execute(Operation.UPDATE, TransactionPhase.POSTCOMMIT, player, null, Optional.empty());

    Message message = rabbitTemplate.receive(TEST_QUEUE, 5000);

    assertThat(message).isNotNull();
    assertThat(message.getMessageProperties().getContentType()).isEqualTo(MessageProperties.CONTENT_TYPE_JSON);
    JSONAssert.assertEquals("""
      {
        "player_id": 42,
        "avatar_id": 5
      }""",
      new String(message.getBody(), StandardCharsets.UTF_8),
      true);
  }

  @Test
  public void clearedAvatarIsPublishedAsJsonWithNull() throws JSONException {
    bindTestQueue();

    Player player = new Player();
    player.setId(42);
    player.setCurrentAvatar(null);

    playerAvatarUpdateHook.execute(Operation.UPDATE, TransactionPhase.POSTCOMMIT, player, null, Optional.empty());

    Message message = rabbitTemplate.receive(TEST_QUEUE, 5000);

    assertThat(message).isNotNull();
    assertThat(message.getMessageProperties().getContentType()).isEqualTo(MessageProperties.CONTENT_TYPE_JSON);
    JSONAssert.assertEquals("""
      {
        "player_id": 42,
        "avatar_id": null
      }""",
      new String(message.getBody(), StandardCharsets.UTF_8),
      true);
  }
}

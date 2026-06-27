package com.faforever.api.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.StatelessRetryOperationsInterceptor;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This configuration assumes a RabbitMQ policy for DLQ to be defined:
 * rabbitmqctl set_policy --vhost "/faf-core" DLX ".*" '{"dead-letter-exchange":"dlx"}' --apply-to queues
 */
@Configuration
@Slf4j
public class RabbitConfiguration {
  public static final String EXCHANGE_DEAD_LETTER = "dlx";
  public static final String EXCHANGE_FAF_LOBBY = "faf-lobby";
  public static final String QUEUE_ACHIEVEMENT = "faf-lobby.api.achievement.update";
  public static final String QUEUE_EVENT = "faf-lobby.api.event.update";
  public static final String QUEUE_ACHIEVEMENT_ROUTING_KEY = "request.achievement.update";
  public static final String QUEUE_EVENT_ROUTING_KEY = "request.event.update";

  /**
   * Define an interceptor that tries to process a message 3 times
   * (afterwards it will be nacked)
   */
  @Bean
  public StatelessRetryOperationsInterceptor retryInterceptor() {
    return RetryInterceptorBuilder.StatelessRetryInterceptorBuilder
      .stateless()
      .maxRetries(2)
      .backOffOptions(1000, 2.0, 10_000)
      .recoverer(new RejectAndDontRequeueRecoverer())
      .build();
  }

  /**
   * Reconfigure default Rabbit container, so it doesn't infinitely requeue.
   * Instead, we use a retry that does a limited requeueing.
   */
  /**
   * JSON message converter used for both publishing (applied to the auto-configured
   * {@link org.springframework.amqp.rabbit.core.RabbitTemplate} by Spring Boot when a single
   * {@link org.springframework.amqp.support.converter.MessageConverter} bean is present) and consuming.
   * Without this, the default {@code SimpleMessageConverter} would Java-serialize outgoing payloads
   * ({@code application/x-java-serialized-object}) instead of producing JSON.
   */
  @Bean
  public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
    return new JacksonJsonMessageConverter();
  }

  @Bean
  public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
    ConnectionFactory connectionFactory,
    StatelessRetryOperationsInterceptor retryInterceptor,
    JacksonJsonMessageConverter jacksonJsonMessageConverter
  ) {
    var factory = new SimpleRabbitListenerContainerFactory();
    factory.setDefaultRequeueRejected(false);
    factory.setConnectionFactory(connectionFactory);
    factory.setAdviceChain(retryInterceptor);
    factory.setMessageConverter(jacksonJsonMessageConverter);

    return factory;
  }

  /**
   * Declare all rabbitmq objects
   */
  @Bean
  public Declarables declarables() {
    log.info("Declaring RabbitMQ resources (creating if missing)");

    var deadLetterExchange = new DirectExchange(EXCHANGE_DEAD_LETTER, true, false);
    var fafLobbyExchange = new TopicExchange(EXCHANGE_FAF_LOBBY, true, false);

    var achievementQueue = new Queue(QUEUE_ACHIEVEMENT, true, false, false);
    var eventQueue = new Queue(QUEUE_EVENT, true, false, false);

    List<Declarable> exchanges = List.of(deadLetterExchange, fafLobbyExchange);
    List<Declarable> queues = List.of(achievementQueue, eventQueue);

    return merge(
      exchanges,
      queues,
      bindWithDlq(fafLobbyExchange, deadLetterExchange, achievementQueue, QUEUE_ACHIEVEMENT_ROUTING_KEY),
      bindWithDlq(fafLobbyExchange, deadLetterExchange, eventQueue, QUEUE_EVENT_ROUTING_KEY)
    );
  }

  /**
   * Creates a binding for a queue along with a matching DLQ and DLQ binding
   */
  private List<Declarable> bindWithDlq(Exchange fromExchange, Exchange dlqExchange, Queue queue, String routingKey) {
    var queueBinding = BindingBuilder.bind(queue)
      .to(fromExchange)
      .with(routingKey)
      .noargs();

    var dlq = QueueBuilder.durable(queue.getName() + ".dlq").build();
    var dlqBinding = BindingBuilder
      .bind(dlq)
      .to(dlqExchange)
      .with(routingKey)
      .and(Map.of("x-dead-letter-exchange", dlqExchange.getName()));

    return List.of(queueBinding, dlq, dlqBinding);
  }

  @SafeVarargs
  private Declarables merge(List<Declarable>... declarables) {
    return new Declarables(
      Arrays.stream(declarables)
        .flatMap(Collection::stream)
        .toList()
    );
  }

}

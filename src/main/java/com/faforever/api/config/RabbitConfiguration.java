package com.faforever.api.config;

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
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This configuration assumes a RabbitMQ policy for DLQ to be defined:
 * rabbitmqctl set_policy --vhost "/faf-core" DLX ".*" '{"dead-letter-exchange":"dlx"}' --apply-to queues
 */
@Configuration
public class RabbitConfiguration {
  public static final String EXCHANGE_DEAD_LETTER = "dlx";
  public static final String EXCHANGE_FAF_LOBBY = "faf-lobby";
  public static final String QUEUE_ACHIEVEMENT = "achievement";
  public static final String QUEUE_ACHIEVEMENT_ROUTING_KEY = "achievement";

  /**
   * Define an interceptor that tries to process a message 3 times
   * (afterwards it will be nacked)
   */
  @Bean
  public RetryOperationsInterceptor retryInterceptor() {
    return RetryInterceptorBuilder.StatelessRetryInterceptorBuilder
      .stateless()
      .maxAttempts(3)
      .backOffOptions(1000, 2.0, 10_000)
      .recoverer(new RejectAndDontRequeueRecoverer())
      .build();
  }

  /**
   * Reconfigure default Rabbit container, so it doesn't infinitely requeue.
   * Instead, we use a retry that does a limited requeueing.
   */
  @Bean
  public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
    ConnectionFactory connectionFactory,
    RetryOperationsInterceptor retryInterceptor
  ) {
    var factory = new SimpleRabbitListenerContainerFactory();
    factory.setDefaultRequeueRejected(false);
    factory.setConnectionFactory(connectionFactory);
    factory.setAdviceChain(retryInterceptor);
    factory.setMessageConverter(new Jackson2JsonMessageConverter());

    return factory;
  }

  /**
   * Declare all rabbitmq objects
   */
  @Bean
  public Declarables declarables() {
    var deadLetterExchange = new DirectExchange(EXCHANGE_DEAD_LETTER, true, false);
    var fafLobbyExchange = new TopicExchange(EXCHANGE_FAF_LOBBY, true, false);

    var achievementQueue = new Queue(QUEUE_ACHIEVEMENT, true, false, false);

    List<Declarable> exchanges = List.of(deadLetterExchange, fafLobbyExchange);
    List<Declarable> queues = List.of(achievementQueue);

    return merge(
      exchanges,
      queues,
      bindWithDlq(fafLobbyExchange, deadLetterExchange, achievementQueue, QUEUE_ACHIEVEMENT_ROUTING_KEY)
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

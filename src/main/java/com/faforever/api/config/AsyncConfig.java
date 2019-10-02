package com.faforever.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@EnableAsync
@Configuration
@RequiredArgsConstructor
public class AsyncConfig extends AsyncConfigurerSupport {

  private final FafApiProperties fafApiProperties;

  @Override
  public Executor getAsyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(fafApiProperties.getAsync().getCorePoolSize());
    executor.setMaxPoolSize(fafApiProperties.getAsync().getMaxPoolSize());
    executor.setQueueCapacity(fafApiProperties.getAsync().getQueueCapacity());
    executor.setThreadNamePrefix("Executor-");
    executor.initialize();
    return executor;
  }
}

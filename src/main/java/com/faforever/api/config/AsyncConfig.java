package com.faforever.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.inject.Inject;
import java.util.concurrent.Executor;

@EnableAsync
@Configuration
public class AsyncConfig extends AsyncConfigurerSupport {

  private final FafApiProperties fafApiProperties;

  @Inject
  public AsyncConfig(FafApiProperties fafApiProperties) {
    this.fafApiProperties = fafApiProperties;
  }

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

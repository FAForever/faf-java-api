package com.faforever.api.config;

import com.faforever.api.user.AnopeUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

@Profile("integration-test")
@Configuration
public class MockConfiguration {
  @Bean
  @Primary
  public AnopeUserRepository anopeUserRepository() {
    return mock(AnopeUserRepository.class);
  }
}

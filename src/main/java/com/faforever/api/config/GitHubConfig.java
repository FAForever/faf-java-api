package com.faforever.api.config;

import org.kohsuke.github.GitHub;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class GitHubConfig {
  @Bean
  @ConditionalOnProperty(value = "faf-api.git-hub.access-token")
  public GitHub gitHub(FafApiProperties fafApiProperties) throws IOException {
    return GitHub.connectUsingOAuth(fafApiProperties.getGitHub().getAccessToken());
  }

  @Bean
  @ConditionalOnProperty(value = "faf-api.git-hub.access-token", havingValue = "false", matchIfMissing = true)
  public GitHub offlineGitHub() {
    return GitHub.offline();
  }
}

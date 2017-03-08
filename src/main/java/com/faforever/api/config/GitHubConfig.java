package com.faforever.api.config;

import org.kohsuke.github.GitHub;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;

@Configuration
public class GitHubConfig {
  @Bean
  @Profile("!dev")
  public GitHub gitHub(FafApiProperties fafApiProperties) throws IOException {
    return GitHub.connectUsingOAuth(fafApiProperties.getGitHub().getAccessToken());
  }

  @Bean
  @Profile("dev")
  public GitHub offlineGitHub() {
    return GitHub.offline();
  }
}

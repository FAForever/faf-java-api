package com.faforever.api.config;

import org.kohsuke.github.GitHub;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class GitHubConfig {
  @Bean
  public GitHub gitHub(FafApiProperties fafApiProperties) throws IOException {
    return GitHub.connectUsingOAuth(fafApiProperties.getGitHub().getAccessToken());
  }
}

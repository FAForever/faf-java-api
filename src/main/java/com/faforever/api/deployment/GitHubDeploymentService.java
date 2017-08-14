package com.faforever.api.deployment;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.FeaturedMod;
import com.faforever.api.featuredmods.FeaturedModService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHDeployment;
import org.kohsuke.github.GHEventPayload.Push;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@Slf4j
public class GitHubDeploymentService {

  private final ApplicationContext applicationContext;
  private final FafApiProperties fafApiProperties;
  private final ObjectMapper objectMapper;
  private final FeaturedModService featuredModService;

  public GitHubDeploymentService(ApplicationContext applicationContext, FafApiProperties fafApiProperties,
                                 ObjectMapper objectMapper, FeaturedModService featuredModService) {
    this.applicationContext = applicationContext;
    this.fafApiProperties = fafApiProperties;
    this.objectMapper = objectMapper;
    this.featuredModService = featuredModService;
  }

  @SneakyThrows
  void createDeploymentIfEligible(Push push) {
    String ref = push.getRef();
    String httpUrl = push.getRepository().gitHttpTransportUrl();
    String branch = push.getRef().replace("refs/heads/", "");

    Optional<FeaturedMod> optional = featuredModService.findByGitUrlAndGitBranch(httpUrl, branch);

    if (!optional.isPresent()) {
      log.warn("No configuration present for repository '{}' and branch '{}'", httpUrl, branch);
      return;
    }

    GHDeployment ghDeployment = push.getRepository().createDeployment(ref)
        .autoMerge(false)
        .environment(fafApiProperties.getGitHub().getDeploymentEnvironment())
        .payload(objectMapper.writeValueAsString(optional.get()))
        .create();

    log.info("Created deployment: {}", ghDeployment);
  }

  @Async
  @SneakyThrows
  void deploy(GHDeployment deployment) {
    String environment = deployment.getEnvironment();
    if (!fafApiProperties.getGitHub().getDeploymentEnvironment().equals(environment)) {
      log.warn("Ignoring deployment for environment: {}", environment);
      return;
    }

    applicationContext.getBean(LegacyFeaturedModDeploymentTask.class)
      .setFeaturedMod(objectMapper.readValue(deployment.getPayload(), FeaturedMod.class))
        .run();
  }
}

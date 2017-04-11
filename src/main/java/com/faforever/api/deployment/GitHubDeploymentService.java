package com.faforever.api.deployment;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.config.FafApiProperties.Deployment.DeploymentConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHDeployment;
import org.kohsuke.github.GHEventPayload.Push;
import org.kohsuke.github.GHEventPayload.Push.PushCommit;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;


@Service
@Slf4j
public class GitHubDeploymentService {

  private final ApplicationContext applicationContext;
  private final FafApiProperties fafApiProperties;
  private final ObjectMapper objectMapper;

  public GitHubDeploymentService(ApplicationContext applicationContext, FafApiProperties fafApiProperties, ObjectMapper objectMapper) {
    this.applicationContext = applicationContext;
    this.fafApiProperties = fafApiProperties;
    this.objectMapper = objectMapper;
  }

  @SneakyThrows
  public void createDeploymentIfEligible(Push push) {
    PushCommit headCommit = push.getCommits().stream()
        .filter(commit -> Objects.equals(commit.getSha(), push.getHead()))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Head commit '" + push.getHead() + "' is not in the list of commits"));

    String ref = push.getRef();
    if (!headCommit.isDistinct()) {
      log.debug("Ignoring non-distinct commit to ref: {}", ref);
      return;
    }
    Optional<DeploymentConfiguration> optional = fafApiProperties.getDeployment().getConfigurations().stream()
        .filter(configuration ->
            push.getRepository().gitHttpTransportUrl().equals(configuration.getRepositoryUrl())
                && push.getRef().replace("refs/heads/", "").equals(configuration.getBranch()))
        .findFirst();

    if (!optional.isPresent()) {
      log.warn("No configuration present for repository '{}' and ref '{}'", push.getRepository().gitHttpTransportUrl(), push.getRef());
      return;
    }

    GHDeployment ghDeployment = push.getRepository().createDeployment(ref)
        .environment(fafApiProperties.getGitHub().getDeploymentEnvironment())
        .payload(objectMapper.writeValueAsString(optional.get()))
        .create();

    log.info("Created deployment: {}", ghDeployment);
  }

  @Async
  @SneakyThrows
  public void deploy(GHDeployment deployment) {
    String environment = deployment.getEnvironment();
    if (!fafApiProperties.getGitHub().getDeploymentEnvironment().equals(environment)) {
      log.warn("Ignoring deployment for environment: {}", environment);
      return;
    }

    applicationContext.getBean(LegacyFeaturedModDeploymentTask.class)
        .setConfiguration(objectMapper.readValue(deployment.getPayload(), DeploymentConfiguration.class))
        .run();
  }
}

package com.faforever.api.deployment;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.FeaturedMod;
import com.faforever.api.featuredmods.FeaturedModService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHDeployment;
import org.kohsuke.github.GHDeploymentState;
import org.kohsuke.github.GHEventPayload.Deployment;
import org.kohsuke.github.GHEventPayload.Push;
import org.kohsuke.github.GHRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;


@Service
@Slf4j
public class GitHubDeploymentService {

  private final ApplicationContext applicationContext;
  private final FafApiProperties fafApiProperties;
  private final FeaturedModService featuredModService;

  public GitHubDeploymentService(ApplicationContext applicationContext, FafApiProperties fafApiProperties,
                                 FeaturedModService featuredModService) {
    this.applicationContext = applicationContext;
    this.fafApiProperties = fafApiProperties;
    this.featuredModService = featuredModService;
  }

  @SneakyThrows
  void createDeploymentIfEligible(Push push) {
    String ref = push.getRef();
    Optional<FeaturedMod> optional = featuredModService.findByGitUrlAndGitBranch(
      push.getRepository().getHttpTransportUrl(),
      push.getRef().replace("refs/heads/", "")
    );

    if (optional.isEmpty()) {
      log.warn("No configuration present for repository '{}' and ref '{}'", push.getRepository().getHttpTransportUrl(), push.getRef());
      return;
    }

    GHDeployment ghDeployment = push.getRepository().createDeployment(ref)
      .autoMerge(false)
      .environment(fafApiProperties.getGitHub().getDeploymentEnvironment())
      .payload(optional.get().getTechnicalName())
      .requiredContexts(List.of())
      .create();

    log.info("Created deployment: {}", ghDeployment);
  }

  @Async
  @SneakyThrows
  @Transactional
  @CacheEvict(value = FeaturedMod.TYPE_NAME, allEntries = true)
  public void deploy(Deployment deployment) {
    GHDeployment ghDeployment = deployment.getDeployment();
    String environment = ghDeployment.getEnvironment();
    String deploymentEnvironment = fafApiProperties.getGitHub().getDeploymentEnvironment();
    if (!deploymentEnvironment.equals(environment)) {
      log.warn("Ignoring deployment for environment '{}' as it does not match the current environment '{}'", environment, deploymentEnvironment);
      return;
    }

    GHRepository repository = deployment.getRepository();
    long deploymentId = ghDeployment.getId();

    try {
      performDeployment(ghDeployment, repository, deploymentId);
    } catch (Exception e) {
      log.error("Deployment failed", e);
      updateDeploymentStatus(deploymentId, repository, GHDeploymentState.FAILURE, e.getMessage());
    }
  }

  private void performDeployment(GHDeployment ghDeployment, GHRepository repository, long deploymentId) {
    String modName = ghDeployment.getPayload();
    FeaturedMod featuredMod = featuredModService.findModByTechnicalName(modName)
      .orElseThrow(() -> new IllegalArgumentException("No such mod: " + modName));

    applicationContext.getBean(LegacyFeaturedModDeploymentTask.class)
      .setStatusDescriptionListener(statusText -> updateDeploymentStatus(deploymentId, repository, GHDeploymentState.PENDING, statusText))
      .setFeaturedMod(featuredMod)
      .run();

    updateDeploymentStatus(deploymentId, repository, GHDeploymentState.SUCCESS, "Successfully deployed");
  }

  @SneakyThrows
  private void updateDeploymentStatus(long deploymentId, GHRepository repository, GHDeploymentState state, String description) {
    log.debug("Updating deployment status to '{}' with description: {}", state, description);
    if (description.length() > 140) {
      log.info("Deployment Status description too long truncating");
      description = description.substring(0, 140);
    }
    repository.getDeployment(deploymentId).createStatus(state)
      .description(description)
      .create();
  }
}

package com.faforever.api.deployment;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.FeaturedMod;
import com.faforever.api.featuredmods.FeaturedModService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHDeployment;
import org.kohsuke.github.GHEventPayload.Push;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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
  public void createDeploymentIfEligible(Push push) {
    String ref = push.getRef();

    Optional<FeaturedMod> optional = featuredModService.findByGitUrlAndGitBranch(
      push.getRepository().gitHttpTransportUrl(),
      push.getRef().replace("refs/heads/", "")
    );

    if (!optional.isPresent()) {
      log.warn("No configuration present for repository '{}' and ref '{}'", push.getRepository().gitHttpTransportUrl(), push.getRef());
      return;
    }

    GHDeployment ghDeployment = push.getRepository().createDeployment(ref)
      .autoMerge(false)
      .environment(fafApiProperties.getGitHub().getDeploymentEnvironment())
      .payload(optional.get().getTechnicalName())
      .create();

    log.info("Created deployment: {}", ghDeployment);
  }

  @Async
  @SneakyThrows
  @CacheEvict(FeaturedMod.TYPE_NAME)
  public void deploy(GHDeployment deployment) {
    String environment = deployment.getEnvironment();
    if (!fafApiProperties.getGitHub().getDeploymentEnvironment().equals(environment)) {
      log.warn("Ignoring deployment for environment: {}", environment);
      return;
    }

    String modName = deployment.getPayload();
    FeaturedMod featuredMod = featuredModService.findModByTechnicalName(modName)
      .orElseThrow(() -> new IllegalArgumentException("No such mod: " + modName));

    applicationContext.getBean(LegacyFeaturedModDeploymentTask.class)
      .setFeaturedMod(featuredMod)
      .run();
  }
}

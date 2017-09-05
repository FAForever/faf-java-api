package com.faforever.api.deployment;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.data.domain.FeaturedMod;
import com.faforever.api.featuredmods.FeaturedModService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.github.GHDeployment;
import org.kohsuke.github.GHDeploymentBuilder;
import org.kohsuke.github.GHEventPayload.Push;
import org.kohsuke.github.GHRepository;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GitHubDeploymentServiceTest {

  private static final String EXAMPLE_REPO_URL = "https://example.com/repo.git";
  private static final String ENVIRONMENT = "junit";
  private GitHubDeploymentService instance;

  private FafApiProperties apiProperties;

  @Mock
  private ApplicationContext applicationContext;
  @Mock
  private FeaturedModService featuredModService;

  @Before
  public void setUp() throws Exception {
    apiProperties = new FafApiProperties();
    instance = new GitHubDeploymentService(applicationContext, apiProperties, featuredModService);
  }

  @Test
  public void createDeploymentIfEligibleNoConfigurationAvailable() throws Exception {
    Push push = mock(Push.class);

    GHRepository repository = mock(GHRepository.class);
    when(repository.gitHttpTransportUrl()).thenReturn(EXAMPLE_REPO_URL);
    when(push.getRepository()).thenReturn(repository);
    when(push.getRef()).thenReturn("refs/heads/junit");

    when(featuredModService.findByGitUrlAndGitBranch(EXAMPLE_REPO_URL, "junit"))
      .thenReturn(Optional.empty());

    instance.createDeploymentIfEligible(push);

    verify(repository, never()).createDeployment(any());
  }

  @Test
  public void createDeploymentIfEligible() throws Exception {
    Push push = mock(Push.class);
    when(push.getRef()).thenReturn("refs/heads/junit");
    when(featuredModService.findByGitUrlAndGitBranch(EXAMPLE_REPO_URL, "junit"))
      .thenReturn(Optional.of(new FeaturedMod().setTechnicalName("faf")));

    GHRepository repository = mock(GHRepository.class);
    when(repository.gitHttpTransportUrl()).thenReturn(EXAMPLE_REPO_URL);

    GHDeploymentBuilder deploymentBuilder = mock(GHDeploymentBuilder.class);
    when(deploymentBuilder.autoMerge(false)).thenReturn(deploymentBuilder);
    when(deploymentBuilder.environment(ENVIRONMENT)).thenReturn(deploymentBuilder);
    when(deploymentBuilder.payload("faf")).thenReturn(deploymentBuilder);
    when(repository.createDeployment("refs/heads/junit")).thenReturn(deploymentBuilder);

    when(push.getRepository()).thenReturn(repository);

    apiProperties.getGitHub().setDeploymentEnvironment(ENVIRONMENT);

    instance.createDeploymentIfEligible(push);

    verify(repository).createDeployment(any());
    verify(deploymentBuilder).autoMerge(false);
    verify(deploymentBuilder).environment(ENVIRONMENT);
    verify(deploymentBuilder).create();
  }

  @Test
  public void deployEnvironmentMismatch() throws Exception {
    apiProperties.getGitHub().setDeploymentEnvironment(ENVIRONMENT);

    instance.deploy(new GHDeployment());

    verify(applicationContext, never()).getBean(LegacyFeaturedModDeploymentTask.class);
  }

  @Test
  public void deployEnvironmentMatch() throws Exception {
    apiProperties.getGitHub().setDeploymentEnvironment(ENVIRONMENT);

    GHDeployment deployment = mock(GHDeployment.class);
    when(deployment.getEnvironment()).thenReturn(ENVIRONMENT);
    when(deployment.getPayload()).thenReturn("faf");

    LegacyFeaturedModDeploymentTask task = mock(LegacyFeaturedModDeploymentTask.class);
    when(task.setFeaturedMod(any())).thenReturn(task);
    when(applicationContext.getBean(LegacyFeaturedModDeploymentTask.class)).thenReturn(task);
    when(featuredModService.findModByTechnicalName("faf")).thenReturn(Optional.of(new FeaturedMod()));

    instance.deploy(deployment);

    verify(task).run();
  }
}

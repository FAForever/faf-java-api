package com.faforever.api.deployment;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.config.FafApiProperties.Deployment.DeploymentConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.github.GHDeployment;
import org.kohsuke.github.GHDeploymentBuilder;
import org.kohsuke.github.GHEventPayload.Push;
import org.kohsuke.github.GHEventPayload.Push.PushCommit;
import org.kohsuke.github.GHRepository;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;

import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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
  private ObjectMapper objectMapper;

  @Before
  public void setUp() throws Exception {
    apiProperties = new FafApiProperties();
    instance = new GitHubDeploymentService(applicationContext, apiProperties, objectMapper);
  }

  @Test(expected = IllegalArgumentException.class)
  public void createDeploymentIfEligibleHeadCommitNotEqualToFirstCommit() throws Exception {
    PushCommit pushCommit = mock(PushCommit.class);
    when(pushCommit.getSha()).thenReturn("111");

    Push push = mock(Push.class);
    when(push.getHead()).thenReturn("222");
    when(push.getCommits()).thenReturn(Collections.singletonList(pushCommit));

    instance.createDeploymentIfEligible(push);
  }

  @Test
  public void createDeploymentIfEligibleDistinctIgnored() throws Exception {
    PushCommit pushCommit = mock(PushCommit.class);
    when(pushCommit.isDistinct()).thenReturn(false);

    Push push = mock(Push.class);
    when(push.getCommits()).thenReturn(Collections.singletonList(pushCommit));

    instance.createDeploymentIfEligible(push);

    verify(push, never()).getRepository();
  }

  @Test
  public void createDeploymentIfEligibleNoConfigurationAvailable() throws Exception {
    PushCommit pushCommit = mock(PushCommit.class);
    when(pushCommit.isDistinct()).thenReturn(true);

    Push push = mock(Push.class);
    when(push.getCommits()).thenReturn(Collections.singletonList(pushCommit));

    GHRepository repository = mock(GHRepository.class);
    when(repository.gitHttpTransportUrl()).thenReturn(EXAMPLE_REPO_URL);
    when(push.getRepository()).thenReturn(repository);

    instance.createDeploymentIfEligible(push);

    verify(repository, never()).createDeployment(any());
  }

  @Test
  public void createDeploymentIfEligible() throws Exception {
    PushCommit pushCommit = mock(PushCommit.class);
    when(pushCommit.isDistinct()).thenReturn(true);

    Push push = mock(Push.class);
    when(push.getRef()).thenReturn("refs/heads/master");
    when(push.getCommits()).thenReturn(Collections.singletonList(pushCommit));

    GHRepository repository = mock(GHRepository.class);
    when(repository.gitHttpTransportUrl()).thenReturn(EXAMPLE_REPO_URL);

    GHDeploymentBuilder deploymentBuilder = mock(GHDeploymentBuilder.class);
    when(deploymentBuilder.autoMerge(false)).thenReturn(deploymentBuilder);
    when(deploymentBuilder.environment(ENVIRONMENT)).thenReturn(deploymentBuilder);
    when(deploymentBuilder.payload(anyString())).thenReturn(deploymentBuilder);
    when(repository.createDeployment("refs/heads/master")).thenReturn(deploymentBuilder);

    when(objectMapper.writeValueAsString(any(DeploymentConfiguration.class))).thenReturn("");

    when(push.getRepository()).thenReturn(repository);

    apiProperties.getGitHub().setDeploymentEnvironment(ENVIRONMENT);
    apiProperties.getDeployment().setConfigurations(Collections.singletonList(
        new DeploymentConfiguration()
            .setBranch("master")
            .setModFilesExtension(".nx2")
            .setModName("faf")
            .setRepositoryUrl(EXAMPLE_REPO_URL)
    ));

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

    LegacyFeaturedModDeploymentTask task = mock(LegacyFeaturedModDeploymentTask.class);
    when(task.setConfiguration(any())).thenReturn(task);
    when(applicationContext.getBean(LegacyFeaturedModDeploymentTask.class)).thenReturn(task);

    instance.deploy(deployment);

    verify(task).run();
  }
}

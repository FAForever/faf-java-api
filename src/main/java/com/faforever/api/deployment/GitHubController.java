package com.faforever.api.deployment;

import com.faforever.api.config.FafApiProperties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GHEventPayload.Deployment;
import org.kohsuke.github.GHEventPayload.Push;
import org.kohsuke.github.GitHub;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping(path = "/gitHub")
@Slf4j
public class GitHubController {

  private static final String HMAC_SHA1 = "HmacSHA1";
  private final GitHubDeploymentService gitHubDeploymentService;
  private final GitHub gitHub;
  private final FafApiProperties apiProperties;

  public GitHubController(GitHubDeploymentService gitHubDeploymentService, GitHub gitHub, FafApiProperties apiProperties) {
    this.gitHubDeploymentService = gitHubDeploymentService;
    this.gitHub = gitHub;
    this.apiProperties = apiProperties;
  }

  @Async
  @RequestMapping(path = "/webhook", method = RequestMethod.POST)
  @SneakyThrows
  public void onPush(@RequestBody String body,
                     @RequestHeader("X-Hub-Signature") String signature,
                     @RequestHeader("X-GitHub-Event") String eventType) {
    verifyRequest(body, signature);
    switch (eventType) {
      case "push":
        gitHubDeploymentService.createDeploymentIfEligible(parseEvent(body, Push.class));
        break;
      case "deployment":
        gitHubDeploymentService.deploy(parseEvent(body, Deployment.class).getDeployment());
        break;
      default:
        log.warn("Unhandled event: " + eventType);
    }
  }

  @SneakyThrows
  private <T extends GHEventPayload> T parseEvent(@RequestBody String body, Class<T> type) {
    return gitHub.parseEventPayload(new StringReader(body), type);
  }

  @SneakyThrows
  private void verifyRequest(String payload, String signature) {
    String secret = apiProperties.getGitHub().getWebhookSecret();
    MacSigner macSigner = new MacSigner(HMAC_SHA1, new SecretKeySpec(secret.getBytes(StandardCharsets.US_ASCII), HMAC_SHA1));

    byte[] content = payload.getBytes(StandardCharsets.US_ASCII);
    // Signature starts with "sha1="
    macSigner.verify(content, DatatypeConverter.parseHexBinary(signature.substring(5)));
  }
}

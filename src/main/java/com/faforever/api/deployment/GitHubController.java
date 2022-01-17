package com.faforever.api.deployment;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.error.ApiException;
import com.faforever.api.error.ErrorCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.kohsuke.github.GHEventPayload;
import org.kohsuke.github.GHEventPayload.Deployment;
import org.kohsuke.github.GHEventPayload.Push;
import org.kohsuke.github.GitHub;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

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
      case "push" -> gitHubDeploymentService.createDeploymentIfEligible(parseEvent(body, Push.class));
      case "deployment" -> gitHubDeploymentService.deploy(parseEvent(body, Deployment.class));
      default -> log.warn("Unhandled event: " + eventType);
    }
  }

  private <T extends GHEventPayload> T parseEvent(@RequestBody String body, Class<T> type) throws IOException {
    return gitHub.parseEventPayload(new StringReader(body), type);
  }

  private void verifyRequest(String payload, String signature) throws DecoderException, NoSuchAlgorithmException, InvalidKeyException {
    String secret = apiProperties.getGitHub().getWebhookSecret();
    Mac mac = Mac.getInstance(HMAC_SHA1);
    mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA1));
    byte[] rawHmac = mac.doFinal(payload.getBytes());
    byte[] expected = Hex.decodeHex(signature.substring(5));

    boolean isValid = Arrays.equals(rawHmac, expected);

    if(!isValid) {
      throw ApiException.of(ErrorCode.VALIDATION_FAILED, "X-Hub-Signature does not match calculated signature from payload");
    }
  }
}

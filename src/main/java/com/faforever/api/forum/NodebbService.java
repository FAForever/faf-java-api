package com.faforever.api.forum;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.user.UserDataSyncService;
import com.faforever.api.user.UserUpdatedEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

@Service
@ConditionalOnProperty(value = "faf-api.nodebb.master-token")
@Slf4j
@RequiredArgsConstructor
public class NodebbService implements UserDataSyncService, InitializingBean {
  private final RestTemplate restTemplate;
  private final FafApiProperties properties;

  @Override
  public void afterPropertiesSet() {
    log.info("NodeBB service initialized");
  }

  @Override
  public void userDataChanged(UserUpdatedEvent event) {
    try {
      getNodebbUserId(event.getId())
        .ifPresentOrElse(
          userId -> {
            updateUsernameData(userId, event);
            updateEmailData(userId, event);
            log.info("User data updated in NodeBB: username={}, email={}", event.getUsername(), event.getEmail());
          },
          () -> log.info("User data not updated in NodeBB (User not found): {}", event)
        );
    } catch (Exception e) {
      log.error("Updating user data in NodeBB failed: {}", event, e);
    }
  }

  private Optional<Integer> getNodebbUserId(int userId) {
    URI uri = UriComponentsBuilder.fromHttpUrl(properties.getNodebb().getBaseUrl())
      // This is not an official NodeBB api url, it's coming from our own sso plugin
      .pathSegment("api", "v3", "plugins", "sso", "user", String.valueOf(userId))
      .queryParam("_uid", getAdminUserId())
      .build()
      .toUri();

    try {
      ResponseEntity<UserResponse> result = restTemplate.exchange(uri, HttpMethod.GET, buildAuthorizedRequest(null), UserResponse.class);
      return Optional.ofNullable(result.getBody()).map(UserResponse::uid);
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        log.debug("User id {} not found in NodeBB. Probably the user never logged in there and has no account there.", userId);
        return Optional.empty();
      }

      throw e;
    }
  }

  private void updateUsernameData(int nodebbUserId, UserUpdatedEvent event) {
    URI uri = UriComponentsBuilder.fromHttpUrl(properties.getNodebb().getBaseUrl())
      .pathSegment("api", "v3", "users", String.valueOf(nodebbUserId))
      .build()
      .toUri();

    var usernameUpdate = new UsernameUpdate(getAdminUserId(), event.getUsername());
    restTemplate.exchange(uri, HttpMethod.PUT, buildAuthorizedRequest(usernameUpdate), Void.class);
    log.debug("Username updated in NodeBB: {}", event);
  }

  private void updateEmailData(int nodebbUserId, UserUpdatedEvent event) {
    URI uri = UriComponentsBuilder.fromHttpUrl(properties.getNodebb().getBaseUrl())
      .pathSegment("api", "v3", "users", String.valueOf(nodebbUserId), "emails")
      .build()
      .toUri();

    var usernameUpdate = new EmailUpdate(getAdminUserId(), event.getEmail(), 1);
    restTemplate.exchange(uri, HttpMethod.POST, buildAuthorizedRequest(usernameUpdate), Void.class);
    log.debug("Email updated in NodeBB: {}", event);
  }

  private String getAdminUserId() {
    return String.valueOf(properties.getNodebb().getAdminUserId());
  }

  private <T> HttpEntity<T> buildAuthorizedRequest(T payload) {
    LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getNodebb().getMasterToken());

    return new HttpEntity<>(payload, headers);
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record UserResponse(Integer uid, String username) {
  }

  private record UsernameUpdate(
    // ID of the user to impersonate for the http call (should be an admin user if username change is disabled)
    String _uid,
    String username
  ) {
  }

  private record EmailUpdate(
    // ID of the user to impersonate for the http call (should be an admin user if username change is disabled)
    String _uid,
    String email,
    // must always be 1
    int skipConfirmation
  ) {
  }
}

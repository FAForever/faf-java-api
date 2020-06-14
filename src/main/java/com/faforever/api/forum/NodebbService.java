package com.faforever.api.forum;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.user.UserDataSyncService;
import com.faforever.api.user.UserUpdatedEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
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
  public void afterPropertiesSet() throws Exception {
    log.info("NodeBB service initialized");
  }

  @Override
  public void userDataChanged(UserUpdatedEvent event) {
    try {
      getNodebbUserId(event.getId())
        .ifPresentOrElse(
          userId -> updateUserData(userId, event),
          () -> log.info("User data not updated in NodeBB (User not found): {}", event)
        );
    } catch (Exception e) {
      log.error("Updating user data in NodeBB failed: {}", event, e);
    }
  }

  private Optional<Integer> getNodebbUserId(int userId) {
    URI uri = UriComponentsBuilder.fromHttpUrl(properties.getNodebb().getBaseUrl())
      // This is not an official NodeBB api url, it's coming from our own sso plugin
      .pathSegment("api", "user", "oauth", String.valueOf(userId))
      .queryParam("_uid", properties.getNodebb().getAdminUserId())
      .build()
      .toUri();

    try {
      ResponseEntity<UserResponse> result = restTemplate.exchange(uri, HttpMethod.GET,
        buildAuthorizedRequest(null), UserResponse.class);
      return Optional.of(result.getBody().uid);
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
        log.debug("User id {} not found in NodeBB. Probably the user never logged in there and has no account there.", userId);
        return Optional.empty();
      }

      throw e;
    }
  }

  private void updateUserData(int nodebbUserId, UserUpdatedEvent event) {
    URI uri = UriComponentsBuilder.fromHttpUrl(properties.getNodebb().getBaseUrl())
      .pathSegment("api", "v2", "users", String.valueOf(nodebbUserId))
      .build()
      .toUri();

    UserUpdate userUpdate = new UserUpdate(String.valueOf(properties.getNodebb().getAdminUserId()),
      event.getUsername(), event.getEmail());
    restTemplate.exchange(uri, HttpMethod.PUT, buildAuthorizedRequest(userUpdate), Void.class);
    log.info("User data updated in NodeBB: {}", event);
  }

  private <T> HttpEntity<T> buildAuthorizedRequest(T payload) {
    LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    headers.add("Authorization", "Bearer " + properties.getNodebb().getMasterToken());

    return new HttpEntity<>(payload, headers);
  }

  @Value
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class UserResponse {
    Integer uid;
    String username;
  }

  @Value
  private class UserUpdate {
    /**
     * ID of the user to impersonate for the http call (should be an admin user if username change is disabled)
     */
    String _uid;

    String username;

    String email;
  }
}

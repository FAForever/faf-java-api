package com.faforever.api.mautic;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.config.FafApiProperties.Mautic;
import com.faforever.api.user.UserUpdatedEvent;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.event.EventListener;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Provides access to a Mautic (Open Source Marketing Automation) instance via REST API.
 */
@Service
@Slf4j
@ConditionalOnProperty(value = "faf-api.mautic.client-id")
public class MauticService {

  private final RestTemplate restTemplate;

  @Inject
  public MauticService(MappingJackson2HttpMessageConverter mauticApiMessageConverter,
                       ResponseErrorHandler mauticApiErrorHandler, FafApiProperties properties) {
    this(mauticApiMessageConverter, mauticApiErrorHandler, properties, new RestTemplateBuilder());
  }

  @VisibleForTesting
  MauticService(MappingJackson2HttpMessageConverter mauticApiMessageConverter,
                ResponseErrorHandler mauticApiErrorHandler, FafApiProperties properties,
                RestTemplateBuilder restTemplateBuilder) {

    Mautic mauticProperties = properties.getMautic();

    restTemplateBuilder = restTemplateBuilder
      .additionalMessageConverters(mauticApiMessageConverter)
      .errorHandler(mauticApiErrorHandler)
      .rootUri(mauticProperties.getBaseUrl());

    // TODO use as soon as this is solved: https://github.com/mautic/mautic/issues/5743
//    ClientCredentialsResourceDetails details = new ClientCredentialsResourceDetails();
//    details.setClientId(mauticProperties.getClientId());
//    details.setClientSecret(mauticProperties.getClientSecret());
//    details.setClientAuthenticationScheme(AuthenticationScheme.header);
//    details.setAccessTokenUri(mauticProperties.getAccessTokenUrl());
//    restOperations = restTemplateBuilder.configure(new OAuth2RestTemplate(details));

    // TODO for now, client ID needs to be a username and client secret the user's password.
    RestTemplateBuilder builder = restTemplateBuilder.basicAuthentication(mauticProperties.getClientId(), mauticProperties.getClientSecret());

    restTemplate = builder.build();
  }

  @EventListener
  public void onUserUpdated(UserUpdatedEvent userUpdatedEvent) {
    Map<String, Object> body = Map.of(
      // These are Mautic default fields. For some reason, these are camel case.
      "email", userUpdatedEvent.getEmail(),
      "ipAddress", userUpdatedEvent.getIpAddress(),
      "lastActive", OffsetDateTime.now(),

      // These are Mautic "custom fields" that need to be created  explicitly. For some reason, these are underscore case by default.
      "faf_user_id", userUpdatedEvent.getId(),
      "faf_username", userUpdatedEvent.getUsername()
    );

    try {
      restTemplate.postForObject("/contacts/new", body, Object.class);
      log.debug("Updated contact in Mautic from event: {}", userUpdatedEvent);
    } catch (Exception e) {
      log.error("Could not update contact in Mautic for user id: {}", userUpdatedEvent.getId(), e);
    }
  }
}

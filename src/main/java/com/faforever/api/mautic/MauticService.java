package com.faforever.api.mautic;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.config.FafApiProperties.Mautic;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Provides access to a Mautic (Open Source Marketing Automation) instance via REST API.
 */
@Service
@ConditionalOnProperty(value = "faf-server.mautic.client-id")
public class MauticService {

  private final RestTemplate restOperations;

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

    restTemplateBuilder
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
    RestTemplateBuilder builder = restTemplateBuilder.basicAuthorization(mauticProperties.getClientId(), mauticProperties.getClientSecret());

    restOperations = builder.build();
  }

  @Async
  public CompletableFuture<Object> createOrUpdateContact(String email, String fafUserId, String fafUserName, String ipAddress, OffsetDateTime lastActive) {
    Map<String, Object> body = new HashMap<>();

    // These are Mautic default fields. For some reason, these are camel case.
    Optional.ofNullable(email).ifPresent(s -> body.put("email", email));
    Optional.ofNullable(ipAddress).ifPresent(s -> body.put("ipAddress", ipAddress));
    Optional.ofNullable(lastActive).ifPresent(s -> body.put("lastActive", lastActive));

    // These are Mautic "custom fields" that need to be created explicitly. For some reason, these are underscore case by default.
    Optional.ofNullable(fafUserId).ifPresent(s -> body.put("faf_user_id", fafUserId));
    Optional.ofNullable(fafUserName).ifPresent(s -> body.put("faf_username", fafUserName));

    return CompletableFuture.completedFuture(restOperations.postForObject("/contacts/new", body, Object.class));
  }
}

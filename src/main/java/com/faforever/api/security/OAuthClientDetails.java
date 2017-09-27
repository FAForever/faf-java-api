package com.faforever.api.security;

import com.faforever.api.client.OAuthClient;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;

/**
 * Wraps an {@link OAuthClient} and maps its fields to Spring's {@link ClientDetails}.
 */
public class OAuthClientDetails extends BaseClientDetails {

  public OAuthClientDetails(OAuthClient oAuthClient) {
    super(oAuthClient.getId(),
      null,
      oAuthClient.getDefaultScope().replace(' ', ','), // stay compatible with Flask API
      "authorization_code,refresh_token,implicit,password,client_credentials",
      null,
      oAuthClient.getRedirectUris().replace(' ', ',')); // stay compatible with Flask API
    setClientSecret(oAuthClient.getClientSecret());
  }
}

package com.faforever.api.security.oauth2;

import com.faforever.api.security.oauth2.domain.OAuthClient;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;

/**
 * Wraps an {@link OAuthClient} and maps its fields to Spring's {@link ClientDetails}.
 */
public class OAuthClientDetails extends BaseClientDetails {

  public OAuthClientDetails(OAuthClient oAuthClient) {
    super(oAuthClient.getId(),
        null,
        oAuthClient.getDefaultScope().replace(' ', ','),
        null,
        null);
  }
}

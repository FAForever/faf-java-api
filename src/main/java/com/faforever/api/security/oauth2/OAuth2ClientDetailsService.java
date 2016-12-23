package com.faforever.api.security.oauth2;

import com.faforever.api.config.FafApiProperties;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;

import javax.inject.Inject;

public class OAuth2ClientDetailsService implements ClientDetailsService {

  private final OAuthClientRepository oAuthClientRepository;
  private final int accessTokenValiditySeconds;
  private final int refreshTokenValiditySeconds;

  @Inject
  public OAuth2ClientDetailsService(OAuthClientRepository oAuthClientRepository, FafApiProperties fafApiProperties) {
    this.oAuthClientRepository = oAuthClientRepository;
    this.accessTokenValiditySeconds = fafApiProperties.getJwt().getAccessTokenValiditySeconds();
    this.refreshTokenValiditySeconds = fafApiProperties.getJwt().getRefreshTokenValiditySeconds();
  }

  @Override
  public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
    OAuthClientDetails clientDetails = new OAuthClientDetails(oAuthClientRepository.findOne(clientId));
    clientDetails.setAccessTokenValiditySeconds(accessTokenValiditySeconds);
    clientDetails.setRefreshTokenValiditySeconds(refreshTokenValiditySeconds);
    return clientDetails;
  }
}

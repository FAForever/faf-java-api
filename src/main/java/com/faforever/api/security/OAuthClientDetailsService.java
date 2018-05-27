package com.faforever.api.security;

import com.faforever.api.client.OAuthClient;
import com.faforever.api.client.OAuthClientRepository;
import com.faforever.api.config.FafApiProperties;
import com.faforever.api.config.FafApiProperties.Jwt;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class OAuthClientDetailsService implements ClientDetailsService {

  public static final String CLIENTS_CACHE_NAME = "OAuthClientDetailsService.oAuthClients";
  private final OAuthClientRepository oAuthClientRepository;
  private final FafApiProperties fafApiProperties;

  public OAuthClientDetailsService(OAuthClientRepository oAuthClientRepository, FafApiProperties fafApiProperties) {
    this.oAuthClientRepository = oAuthClientRepository;
    this.fafApiProperties = fafApiProperties;
  }

  @Override
  @Cacheable(CLIENTS_CACHE_NAME)
  public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
    OAuthClient oAuthClient = Optional.ofNullable(oAuthClientRepository.findOne(clientId))
        .orElseThrow(() -> new ClientRegistrationException("Unknown client: " + clientId));

    OAuthClientDetails clientDetails = new OAuthClientDetails(oAuthClient);

    Jwt jwt = fafApiProperties.getJwt();
    clientDetails.setAccessTokenValiditySeconds(jwt.getAccessTokenValiditySeconds());
    clientDetails.setRefreshTokenValiditySeconds(jwt.getRefreshTokenValiditySeconds());

    return clientDetails;
  }
}

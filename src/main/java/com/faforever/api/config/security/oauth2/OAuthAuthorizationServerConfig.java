package com.faforever.api.config.security.oauth2;

import com.faforever.api.client.OAuthClientRepository;
import com.faforever.api.config.FafApiProperties;
import com.faforever.api.security.FafUserDetailsService;
import com.faforever.api.security.OAuthClientDetailsService;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenStore;

import javax.inject.Inject;

/**
 * OAuth2 authorization server configuration.
 */
@Configuration
@EnableAuthorizationServer
public class OAuthAuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

  private final AuthenticationManager authenticationManager;
  private final TokenStore tokenStore;
  private final TokenEnhancer tokenEnhancer;
  private final FafUserDetailsService userDetailsService;
  private final OAuthClientRepository oAuthClientRepository;
  private FafApiProperties properties;

  @Inject
  public OAuthAuthorizationServerConfig(AuthenticationManager authenticationManager, TokenStore tokenStore,
                                        TokenEnhancer tokenEnhancer, FafUserDetailsService userDetailsService,
                                        OAuthClientRepository oAuthClientRepository, FafApiProperties properties) {
    this.authenticationManager = authenticationManager;
    this.tokenStore = tokenStore;
    this.tokenEnhancer = tokenEnhancer;
    this.userDetailsService = userDetailsService;
    this.oAuthClientRepository = oAuthClientRepository;
    this.properties = properties;
  }

  @Override
  public void configure(AuthorizationServerSecurityConfigurer oAuthServer) throws Exception {
    oAuthServer
        .tokenKeyAccess("isAnonymous() || hasAuthority('ROLE_TRUSTED_CLIENT')")
        .checkTokenAccess("hasAuthority('ROLE_TRUSTED_CLIENT')");
  }

  @Override
  public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
    clients.withClientDetails(new OAuthClientDetailsService(oAuthClientRepository, properties));
  }

  @Override
  public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
    endpoints
        .userDetailsService(userDetailsService)
        .tokenStore(tokenStore)
        .tokenEnhancer(tokenEnhancer)
        .authenticationManager(authenticationManager);
  }
}

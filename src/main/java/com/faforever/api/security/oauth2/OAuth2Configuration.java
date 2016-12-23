package com.faforever.api.security.oauth2;

import com.faforever.api.config.FafApiProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import javax.inject.Inject;

/**
 * OAuth2 authorization server configuration.
 */
@Configuration
@EnableAuthorizationServer
public class OAuth2Configuration extends AuthorizationServerConfigurerAdapter {

  private final AuthenticationManager authenticationManager;
  private final OAuthClientRepository oAuthClientRepository;
  private final FafApiProperties fafApiProperties;

  @Inject
  public OAuth2Configuration(AuthenticationManager authenticationManager, OAuthClientRepository oAuthClientRepository, FafApiProperties fafApiProperties) {
    this.authenticationManager = authenticationManager;
    this.oAuthClientRepository = oAuthClientRepository;
    this.fafApiProperties = fafApiProperties;
  }

  @Override
  public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
    oauthServer
        .tokenKeyAccess("isAnonymous() || hasAuthority('ROLE_TRUSTED_CLIENT')")
        .checkTokenAccess("hasAuthority('ROLE_TRUSTED_CLIENT')");
  }

  @Override
  public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
    clients.withClientDetails(new OAuth2ClientDetailsService(oAuthClientRepository, fafApiProperties));
  }

  @Override
  public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
    endpoints
        .tokenStore(tokenStore())
        .tokenEnhancer(jwtTokenConverter())
        .authenticationManager(this.authenticationManager);
  }

  @Bean
  public TokenStore tokenStore() {
    return new JwtTokenStore(jwtTokenConverter());
  }

  @Bean
  protected JwtAccessTokenConverter jwtTokenConverter() {
    return new JwtAccessTokenConverter();
  }
}

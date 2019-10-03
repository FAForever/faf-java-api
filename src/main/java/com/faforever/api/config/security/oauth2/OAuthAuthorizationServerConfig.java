package com.faforever.api.config.security.oauth2;

import com.faforever.api.security.FafUserDetailsService;
import com.faforever.api.security.OAuthClientDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenStore;

/**
 * OAuth2 authorization server configuration.
 */
@Configuration
@EnableAuthorizationServer
@RequiredArgsConstructor
public class OAuthAuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

  private final AuthenticationManager authenticationManager;
  private final TokenStore tokenStore;
  private final TokenEnhancer tokenEnhancer;
  private final FafUserDetailsService userDetailsService;
  private final OAuthClientDetailsService oAuthClientDetailsService;

  @Override
  public void configure(AuthorizationServerSecurityConfigurer oAuthServer) throws Exception {
    final OAuth2AuthenticationEntryPoint oAuth2AuthenticationEntryPoint = new OAuth2AuthenticationEntryPoint();
    oAuth2AuthenticationEntryPoint.setExceptionRenderer(new JsonApiOauthExceptionRenderer());
    oAuthServer
      .tokenKeyAccess("isAnonymous() || hasAuthority('ROLE_TRUSTED_CLIENT')")
      .checkTokenAccess("hasAuthority('ROLE_TRUSTED_CLIENT')")
      .authenticationEntryPoint(oAuth2AuthenticationEntryPoint)
      .allowFormAuthenticationForClients();
  }

  @Override
  public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
    clients.withClientDetails(oAuthClientDetailsService);
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

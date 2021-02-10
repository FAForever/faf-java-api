package com.faforever.api.security;

import com.faforever.api.config.FafApiProperties;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.jwk.JwkTokenStore;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;

@Component
public class FafMultiTokenStore implements TokenStore {
  /**
   * The token store for token created by this API (using custom OAuth 2.0 without OpenID Connect)
   */
  private final JwtTokenStore classicTokenStore;

  /**
   * The token store for tokens created by Ory Hydra (OpenID Connect token customized for FAF)
   */
  private final JwkTokenStore hydraTokenStore;

  private final FafApiProperties fafApiProperties;

  public FafMultiTokenStore(FafApiProperties fafApiProperties, JwtAccessTokenConverter jwtAccessTokenConverter) {
    this.fafApiProperties = fafApiProperties;

    classicTokenStore = new JwtTokenStore(jwtAccessTokenConverter);
    hydraTokenStore = new JwkTokenStore(fafApiProperties.getJwt().getHydraJwksUrl(), jwtAccessTokenConverter);
  }

  @Override
  public OAuth2Authentication readAuthentication(OAuth2AccessToken token) {
    return readAuthentication(token.getValue());
  }

  @Override
  public OAuth2Authentication readAuthentication(String token) {
    try {
      return classicTokenStore.readAuthentication(token);
    } catch (Exception e) {
      return hydraTokenStore.readAuthentication(token);
    }
  }

  @Override
  public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
    // no implementation, equal to JwtTokenStore
  }

  @Override
  public OAuth2AccessToken readAccessToken(String tokenValue) {
    try {
      return classicTokenStore.readAccessToken(tokenValue);
    } catch (Exception e) {
      return hydraTokenStore.readAccessToken(tokenValue);
    }
  }

  @Override
  public void removeAccessToken(OAuth2AccessToken token) {
    // no implementation, equal to JwtTokenStore
  }

  @Override
  public void storeRefreshToken(OAuth2RefreshToken refreshToken, OAuth2Authentication authentication) {
    // no implementation, equal to JwtTokenStore
  }

  @Override
  public OAuth2RefreshToken readRefreshToken(String tokenValue) {
    return null;
  }

  @Override
  public OAuth2Authentication readAuthenticationForRefreshToken(OAuth2RefreshToken token) {
    return null;
  }

  @Override
  public void removeRefreshToken(OAuth2RefreshToken token) {
    // no implementation, equal to JwtTokenStore
  }

  @Override
  public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken refreshToken) {
    // no implementation, equal to JwtTokenStore
  }

  @Override
  public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
    // equal to JwtTokenStore
    return null;
  }

  @Override
  public Collection<OAuth2AccessToken> findTokensByClientIdAndUserName(String clientId, String userName) {
    // equal to JwtTokenStore
    return Collections.emptySet();
  }

  @Override
  public Collection<OAuth2AccessToken> findTokensByClientId(String clientId) {
    // equal to JwtTokenStore
    return Collections.emptySet();
  }
}

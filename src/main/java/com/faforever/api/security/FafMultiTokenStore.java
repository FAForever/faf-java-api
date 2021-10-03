package com.faforever.api.security;

import com.faforever.api.config.FafApiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.common.util.JsonParser;
import org.springframework.security.oauth2.common.util.JsonParserFactory;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.jwk.JwkTokenStore;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class FafMultiTokenStore implements TokenStore {
  private JsonParser objectMapper = JsonParserFactory.create();

  /**
   * The token store for token created by this API (using custom OAuth 2.0 without OpenID Connect)
   */
  private final JwtTokenStore classicTokenStore;

  /**
   * The token store for tokens created by Ory Hydra (OpenID Connect token customized for FAF)
   */
  private final JwkTokenStore hydraTokenStore;

  private final FafApiProperties fafApiProperties;

  public FafMultiTokenStore(FafApiProperties fafApiProperties,
                            @Qualifier("classicAccessTokenConverter")
                            JwtAccessTokenConverter jwtAccessTokenConverter,
                            @Qualifier("hydraAccessTokenConverter")
                            JwtAccessTokenConverter hydraAccessTokenConverter) {
    this.fafApiProperties = fafApiProperties;

    classicTokenStore = new JwtTokenStore(jwtAccessTokenConverter);
    hydraTokenStore = new JwkTokenStore(fafApiProperties.getJwt().getFafHydraJwksUrl(), hydraAccessTokenConverter);
  }

  @Override
  public OAuth2Authentication readAuthentication(OAuth2AccessToken token) {
    return readAuthentication(token.getValue());
  }

  @Override
  public OAuth2Authentication readAuthentication(String token) {
    Jwt unverifiedJwt = JwtHelper.decode(token);
    Map<String, Object> claims = objectMapper.parseMap(unverifiedJwt.getClaims());

    if (Objects.equals(claims.get("iss"), fafApiProperties.getJwt().getFafHydraIssuer())) {
      log.trace("Reading authentication for Hydra token");
      return hydraTokenStore.readAuthentication(token);
    } else {
      log.trace("Reading authentication for legacy API token");
      return classicTokenStore.readAuthentication(token);
    }
  }

  @Override
  public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
    // no implementation, equal to JwtTokenStore
  }

  @Override
  public OAuth2AccessToken readAccessToken(String tokenValue) {
    try {
      log.trace("Reading access token for legacy api token");
      return classicTokenStore.readAccessToken(tokenValue);
    } catch (Exception e) {
      log.trace("Reading access token for legacy api token failed. Fallback to reading access token for Hydra token");
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
    return List.of();
  }

  @Override
  public Collection<OAuth2AccessToken> findTokensByClientId(String clientId) {
    // equal to JwtTokenStore
    return List.of();
  }
}

package com.faforever.api.config.security.oauth2;

import com.faforever.api.config.FafApiProperties;
import com.faforever.api.security.FafUserAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import java.io.IOException;
import java.nio.file.Files;

@Configuration
public class OAuthJwtConfig {

  private final FafApiProperties fafApiProperties;

  public OAuthJwtConfig(FafApiProperties fafApiProperties) {
    this.fafApiProperties = fafApiProperties;
  }

  @Bean
  @Primary
  public DefaultTokenServices tokenServices(TokenStore tokenStore) {
    DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
    defaultTokenServices.setTokenStore(tokenStore);
    defaultTokenServices.setSupportRefreshToken(true);
    return defaultTokenServices;
  }

  @Bean
  public TokenStore tokenStore(JwtAccessTokenConverter jwtAccessTokenConverter) {
    return new JwtTokenStore(jwtAccessTokenConverter);
  }

  @Bean
  protected JwtAccessTokenConverter jwtAccessTokenConverter() throws IOException {
    String secretKey = Files.readString(fafApiProperties.getJwt().getSecretKeyPath());
    String publicKey = Files.readString(fafApiProperties.getJwt().getPublicKeyPath());

    JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
    jwtAccessTokenConverter.setSigningKey(secretKey);
    jwtAccessTokenConverter.setVerifierKey(publicKey);
    ((DefaultAccessTokenConverter) jwtAccessTokenConverter.getAccessTokenConverter()).setUserTokenConverter(new FafUserAuthenticationConverter());
    return jwtAccessTokenConverter;
  }
}

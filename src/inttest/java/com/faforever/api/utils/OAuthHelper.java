package com.faforever.api.utils;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Set;

@Component
public class OAuthHelper {

  @Autowired
  AuthorizationServerTokenServices tokenservice;

  @Autowired
  JwtAccessTokenConverter jwtAccessTokenConverter;

  public RequestPostProcessor addBearerToken(Set<String> scope) {
    return mockRequest -> {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      OAuth2Request oauth2Request = createOAuth2Request(scope);
      OAuth2Authentication oauth2auth = new OAuth2Authentication(oauth2Request, authentication);
      OAuth2AccessToken token = tokenservice.createAccessToken(oauth2auth);
      token = jwtAccessTokenConverter.enhance(token, oauth2auth);

      // Set Authorization header to use Bearer
      mockRequest.addHeader("Authorization", "Bearer " + token.getValue());
      return mockRequest;
    };
  }

  @NotNull
  private OAuth2Request createOAuth2Request(Set<String> scope) {
    return new OAuth2Request(null, "test", null, true, scope, null, null, null, null);
  }

}

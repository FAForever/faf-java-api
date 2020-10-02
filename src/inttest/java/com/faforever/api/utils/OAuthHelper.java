package com.faforever.api.utils;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static com.faforever.api.security.FafUserAuthenticationConverter.USER_ID_KEY;
import static org.springframework.security.oauth2.provider.token.UserAuthenticationConverter.USERNAME;

@Component
public class OAuthHelper {

  @Autowired
  AuthorizationServerTokenServices tokenServices;

  @Autowired
  JwtAccessTokenConverter jwtAccessTokenConverter;


  public RequestPostProcessor addBearerToken(Set<String> scope, Set<String> authorities) {
    return addBearerToken(null, null, scope, authorities);
  }

  public RequestPostProcessor addBearerToken(Integer userId, String userName, Set<String> scope, Set<String> authorities) {
    return mockRequest -> {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      OAuth2Request oauth2Request = createOAuth2Request(scope, authorities);
      OAuth2Authentication oauth2auth = new OAuth2Authentication(oauth2Request, authentication);
      DefaultOAuth2AccessToken token = (DefaultOAuth2AccessToken) tokenServices.createAccessToken(oauth2auth);
      token.setAdditionalInformation(new HashMap<>(token.getAdditionalInformation()));
      if (userId != null) {
        token.getAdditionalInformation().put(USER_ID_KEY, userId);
      }

      if (userName != null) {
        token.getAdditionalInformation().put(USERNAME, userName);
      }

      token = (DefaultOAuth2AccessToken) jwtAccessTokenConverter.enhance(token, oauth2auth);

      // Set Authorization header to use Bearer
      mockRequest.addHeader("Authorization", "Bearer " + token.getValue());
      return mockRequest;
    };
  }

  @NotNull
  private OAuth2Request createOAuth2Request(Set<String> scope, Set<String> authorities) {
    List<GrantedAuthority> grantedAuthorities = authorities == null ? null : AuthorityUtils.createAuthorityList(authorities.toArray(new String[0]));
    return new OAuth2Request(null, "00000000-0000-0000-0000-000000000000", grantedAuthorities, true, scope, null, null, null, null);
  }
}

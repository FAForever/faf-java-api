package com.faforever.api.config.security.oauth2;

import com.faforever.api.config.FafApiProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

/**
 * OAuth2 resource server configuration.
 */
@Configuration
@EnableResourceServer
public class OAuthResourceServerConfig extends ResourceServerConfigurerAdapter {

  private static class OAuthRequestedMatcher implements RequestMatcher {

    public boolean matches(HttpServletRequest request) {
      String auth = request.getHeader("Authorization");
      boolean hasOauth2Token = (auth != null) && auth.startsWith("Bearer");
      boolean hasAccessToken = request.getParameter("access_token") != null;
      return hasOauth2Token || hasAccessToken;
    }
  }

  private final String resourceId;
  private final ResourceServerTokenServices tokenServices;

  @Inject
  public OAuthResourceServerConfig(FafApiProperties fafApiProperties, ResourceServerTokenServices tokenServices) {
    this.resourceId = fafApiProperties.getOAuth2().getResourceId();
    this.tokenServices = tokenServices;
  }

  @Override
  public void configure(ResourceServerSecurityConfigurer resources) {
    resources.resourceId(resourceId)
        .tokenServices(tokenServices);
  }

  @Override
  public void configure(HttpSecurity http) throws Exception {
    http.requestMatcher(new OAuthRequestedMatcher())
        .authorizeRequests()
        .antMatchers(HttpMethod.OPTIONS).permitAll()
        .anyRequest().authenticated();
  }
}

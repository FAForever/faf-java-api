package com.faforever.api.config.security;

import com.faforever.api.security.FafAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.ExceptionMappingAuthenticationFailureHandler;

import java.util.Map;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    final var bearerTokenResolver = new DefaultBearerTokenResolver();
    bearerTokenResolver.setAllowUriQueryParameter(true);

    http.headers(headersConfig -> headersConfig.cacheControl(HeadersConfigurer.CacheControlConfig::disable));
    http.formLogin(AbstractHttpConfigurer::disable);
    http.oauth2ResourceServer(oauth2Config -> {
      oauth2Config.bearerTokenResolver(bearerTokenResolver);
      oauth2Config.jwt(jwtConfig -> jwtConfig.jwtAuthenticationConverter(new FafAuthenticationConverter()));
    });
    http.authorizeRequests(authorizeConfig -> {
      authorizeConfig.requestMatchers(HttpMethod.OPTIONS).permitAll();
      // Swagger UI
      authorizeConfig.requestMatchers(
        "/swagger-ui/**",
        "/swagger-resources/**",
        "/v3/api-docs/**",
        "/"
      ).permitAll();
      // Webapp folder
      authorizeConfig.requestMatchers(
        "/css/*",
        "/favicon.ico",
        "/robots.txt"
      ).permitAll();
    });
    // @formatter:on
    return http.build();
  }

  @Bean
  public AuthenticationFailureHandler authenticationFailureHandler() {
    Map<Object, String> exceptionMappings = Map.of(
      InternalAuthenticationServiceException.class.getCanonicalName(), "/login?error=serverError",
      BadCredentialsException.class.getCanonicalName(), "/login?error=badCredentials",
      LockedException.class.getCanonicalName(), "/login?error=locked"
    );

    final ExceptionMappingAuthenticationFailureHandler result = new ExceptionMappingAuthenticationFailureHandler();
    result.setExceptionMappings(exceptionMappings);
    result.setDefaultFailureUrl("/login?error=unknown");
    return result;
  }
}

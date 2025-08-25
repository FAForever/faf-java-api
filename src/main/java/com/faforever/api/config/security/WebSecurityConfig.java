package com.faforever.api.config.security;

import com.faforever.api.security.FafAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    final var bearerTokenResolver = new DefaultBearerTokenResolver();
    bearerTokenResolver.setAllowUriQueryParameter(true);

    return http.headers(headersConfig ->
        headersConfig.cacheControl(HeadersConfigurer.CacheControlConfig::disable)
      )
      .formLogin(AbstractHttpConfigurer::disable)
      .oauth2ResourceServer(oauth2Config ->
        oauth2Config
          .bearerTokenResolver(bearerTokenResolver)
          .jwt(jwtConfig -> jwtConfig.jwtAuthenticationConverter(new FafAuthenticationConverter()))
      )
      .authorizeHttpRequests(authorizeConfig ->
        authorizeConfig
          .requestMatchers(HttpMethod.OPTIONS).permitAll()
          // Swagger UI
          .requestMatchers("/swagger-ui/**", "/swagger-resources/**", "/v3/api-docs/**", "/").permitAll()
          // Webapp folder
          .requestMatchers("/css/*", "/favicon.ico", "/robots.txt").permitAll()).build();
  }
}

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
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.ExceptionMappingAuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.regex.Pattern;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    final var bearerTokenResolver = new DefaultBearerTokenResolver();
    bearerTokenResolver.setAllowUriQueryParameter(true);

    // @formatter:off
    http.csrf(csrfConfig -> csrfConfig.requireCsrfProtectionMatcher(new RequestMatcher() {
      private Pattern allowedMethods = Pattern.compile("^(GET|HEAD|TRACE|OPTIONS)$");
      private RequestMatcher matcher = new OrRequestMatcher(
        new AntPathRequestMatcher("/oauth/authorize"),
        new AntPathRequestMatcher("/login"));

      @Override
      public boolean matches(HttpServletRequest request) {
        return matcher.matches(request) && !allowedMethods.matcher(request.getMethod()).matches();
      }
    }));
    http.headers(headersConfig -> headersConfig.cacheControl().disable());
    http.formLogin().disable();
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

package com.faforever.api.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.MessageDigestPasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.ExceptionMappingAuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.regex.Pattern;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  @Inject
  public void prodUserDetails(AuthenticationManagerBuilder auth, UserDetailsService userDetailsService) throws Exception {
    // TODO: Use a proper, backward-compatible password encryption

    auth
      .userDetailsService(userDetailsService)
      .passwordEncoder(new MessageDigestPasswordEncoder("SHA-256"));
  }

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // @formatter:off
      http
        .csrf()
          .requireCsrfProtectionMatcher(new RequestMatcher() {
            private Pattern allowedMethods = Pattern.compile("^(GET|HEAD|TRACE|OPTIONS)$");
            private RequestMatcher matcher = new OrRequestMatcher(
              new AntPathRequestMatcher("/oauth/authorize"),
              new AntPathRequestMatcher("/login"));

            @Override
            public boolean matches(HttpServletRequest request) {
                return matcher.matches(request) && !allowedMethods.matcher(request.getMethod()).matches();
            }
        })
        .and().headers()
        .cacheControl().disable()
        .and().formLogin()
          .loginPage("/login").permitAll()
          .failureHandler(authenticationFailureHandler())
        .and().authorizeRequests()
          .antMatchers(HttpMethod.OPTIONS).permitAll()
          // Swagger UI
          .antMatchers("/swagger-ui").permitAll()
          .antMatchers("/swagger-resources/**").permitAll()
          .antMatchers("/v2/api-docs/**").permitAll()
          .antMatchers("/").permitAll()
          // Webapp folder
          .antMatchers("/css/*").permitAll()
          .antMatchers("/favicon.ico").permitAll()
          .antMatchers("/robots.txt").permitAll();
    // @formatter:on
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

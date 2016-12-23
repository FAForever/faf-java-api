package com.faforever.api.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JWTLoginFilter extends AbstractAuthenticationProcessingFilter {

  private final ObjectMapper objectMapper;
  private final TokenAuthenticationService tokenAuthenticationService;

  public JWTLoginFilter(String url, AuthenticationManager authenticationManager, ObjectMapper objectMapper, TokenAuthenticationService tokenAuthenticationService) {
    super(new AntPathRequestMatcher(url));
    this.objectMapper = objectMapper;
    this.tokenAuthenticationService = tokenAuthenticationService;
    setAuthenticationManager(authenticationManager);
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
      throws AuthenticationException, IOException, ServletException {
    AccountCredentials credentials = objectMapper.readValue(httpServletRequest.getInputStream(), AccountCredentials.class);
    UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(credentials.getUsername(), credentials.getPassword());
    return getAuthenticationManager().authenticate(token);
  }

  @Override
  protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication)
      throws IOException, ServletException {
    String name = authentication.getName();
    tokenAuthenticationService.addAuthentication(response, name);
  }
}

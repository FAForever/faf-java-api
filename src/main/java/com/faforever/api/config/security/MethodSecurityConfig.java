package com.faforever.api.config.security;

import com.faforever.api.security.method.CustomMethodSecurityExpressionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {
  @Override
  protected MethodSecurityExpressionHandler createExpressionHandler() {
    return new CustomMethodSecurityExpressionHandler();
  }
}

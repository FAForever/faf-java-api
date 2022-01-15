package com.faforever.api.config.security;

import com.faforever.api.security.method.CustomMethodSecurityExpressionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {
  @Override
  protected MethodSecurityExpressionHandler createExpressionHandler() {
    return new CustomMethodSecurityExpressionHandler();
  }
}

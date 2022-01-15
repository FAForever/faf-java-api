package com.faforever.api.security.method;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;

/**
 * Wraps the CustomMethodSecurityExpressionRoot into an expression handler
 */
public class CustomMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {
  private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

  @Override
  protected MethodSecurityExpressionOperations createSecurityExpressionRoot(Authentication authentication, MethodInvocation invocation) {
    var root = new CustomMethodSecurityExpressionRoot(authentication);

    root.setPermissionEvaluator(getPermissionEvaluator());
    root.setTrustResolver(trustResolver);
    root.setRoleHierarchy(getRoleHierarchy());

    return root;
  }
}

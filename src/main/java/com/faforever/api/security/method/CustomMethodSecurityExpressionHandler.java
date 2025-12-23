package com.faforever.api.security.method;

import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.Nullable;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;

import java.util.function.Supplier;

/**
 * Wraps the CustomMethodSecurityExpressionRoot into an expression handler
 */
public class CustomMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {
  private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

  @Override
  public EvaluationContext createEvaluationContext(Supplier<? extends @Nullable Authentication> authentication, MethodInvocation mi) {
    StandardEvaluationContext context = (StandardEvaluationContext) super.createEvaluationContext(authentication, mi);
    var root = new CustomMethodSecurityExpressionRoot(authentication);

    root.setPermissionEvaluator(getPermissionEvaluator());
    root.setTrustResolver(trustResolver);
    root.setRoleHierarchy(getRoleHierarchy());
    context.setRootObject(root);
    return context;
  }
}

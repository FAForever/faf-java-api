package com.faforever.api.security;

import com.yahoo.elide.core.audit.InvalidSyntaxException;
import de.odysseus.el.ExpressionFactoryImpl;
import de.odysseus.el.util.SimpleContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.el.ELException;
import javax.el.ExpressionFactory;
import java.text.MessageFormat;

@Aspect
@Slf4j
@Component
public class AuditAspect {
  private static final ExpressionFactory EXPRESSION_FACTORY = new ExpressionFactoryImpl();
  private AuditService auditService;

  public AuditAspect(AuditService auditService) {
    this.auditService = auditService;
  }

  @Around("execution(* *.*(..)) && @annotation(auditAnnotation)")
  public Object auditLog(ProceedingJoinPoint pjp, Audit auditAnnotation) throws Throwable {
    MethodSignature signature = (MethodSignature) pjp.getSignature();
    final SimpleContext context = new SimpleContext();

    for (int i = 0; i < signature.getParameterNames().length; i++) {
      context.setVariable(signature.getParameterNames()[i], EXPRESSION_FACTORY.createValueExpression(pjp.getArgs()[i], Object.class));
    }

    final String[] eventDataExpressions = auditAnnotation.expressions();
    Object[] eventData = new Object[eventDataExpressions.length];
    for (int i = 0; i < eventDataExpressions.length; i++) {
      try {
        eventData[i] = EXPRESSION_FACTORY.createValueExpression(context, eventDataExpressions[i], Object.class).getValue(context);
      } catch (ELException e) {
        throw new InvalidSyntaxException(e);
      }
    }

    Object call = pjp.proceed();
    auditService.logMessage(MessageFormat.format(auditAnnotation.messageTemplate(), eventData));
    return call;
  }
}
